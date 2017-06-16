package com.brandon3055.brandonscore.registry;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import com.brandon3055.brandonscore.client.gui.config.GuiIncompatibleConfig;
import com.brandon3055.brandonscore.registry.ModConfigProperty.ListRestrictions;
import com.brandon3055.brandonscore.registry.ModConfigProperty.MinMax;
import com.brandon3055.brandonscore.registry.ModConfigProperty.ValidValues;
import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.google.common.base.Throwables;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.brandon3055.brandonscore.registry.ModFeatureParser.CATEGORY_BLOCKS;
import static com.brandon3055.brandonscore.registry.ModFeatureParser.CATEGORY_ITEMS;

/**
 * Created by brandon3055 on 24/3/2016.
 * This is based on the config system in Open Mods but almost completely rewritten and simplified
 */
public class ModConfigParser {

    private static Map<String, Configuration> modConfigurations = new HashMap<>();
    private static Map<String, IModConfigHelper> modConfigHelpers = new HashMap<>();
    private static Map<String, List<PropertyHelper>> modPropertyHelpers = new HashMap<>();
    private static Map<String, List<ConfigCategory>> modConfigCategories = new HashMap<>();
    private static boolean connectedToServer = false;

    //region Pars & Config

    /**
     * Parses the ASMDataTable and finds all defined mod config classes
     */
    public static void parseASMData(ASMDataTable table) {
        String modid = "unknown";
        for (ASMDataTable.ASMData data : table.getAll(ModConfigContainer.class.getName())) {
            try {
                Class clazz = Class.forName(data.getClassName());
                ModConfigContainer config = (ModConfigContainer) clazz.getAnnotation(ModConfigContainer.class);
                modid = config.modid();

                if (IModConfigHelper.class.isAssignableFrom(clazz)) {
                    if (modConfigHelpers.containsKey(modid)) {
                        throw new RuntimeException("Mod: " + modid + " Attempted to register more than 1 IModConfigHelper class. THERE CAN ONLY BE ONE!!!");
                    }
                    modConfigHelpers.put(modid, (IModConfigHelper) clazz.newInstance());
                }

                List<PropertyHelper> modProps = modPropertyHelpers.computeIfAbsent(modid, s -> new ArrayList<>());
                LogHelperBC.info("Found mod config container for mod: " + modid);
                for (Field field : clazz.getFields()) {
                    try {
                        if (field.isAnnotationPresent(ModConfigProperty.class)) {
                            PropertyHelper container = new PropertyHelper(modid, field, field.getAnnotation(ModConfigProperty.class));
                            modProps.add(container);
                        }
                    }
                    catch (Throwable e) {
                        LogHelperBC.error("An error occurred while attempting to parse feature " + field.getName() + " from mod " + modid);
                        e.printStackTrace();
                    }
                }
            }
            catch (Throwable e) {
                LogHelperBC.error("An error occurred while attempting to load mod features for mod " + modid);
                Throwables.propagate(e);
            }
        }

        DataUtils.forEachMatch(modPropertyHelpers.keySet(), mod -> !modConfigHelpers.containsKey(mod), mod -> {
            throw new RuntimeException("No IModConfigHelper was found for mod " + mod);
        });
    }

    public static void loadConfigs(FMLPreInitializationEvent event) {
        modConfigHelpers.forEach((modid, configHelper) -> modConfigurations.put(modid, configHelper.createConfiguration(event)));

        for (String modid : modPropertyHelpers.keySet()) {
            Configuration config = modConfigurations.get(modid);
            List<ConfigCategory> cats = modConfigCategories.computeIfAbsent(modid, id -> new ArrayList<>());
            for (PropertyHelper helper : modPropertyHelpers.get(modid)) {
                try {
                    if (!cats.contains(config.getCategory(helper.category))) {
                        cats.add(config.getCategory(helper.category));
                        config.setCategoryComment(helper.category, modConfigHelpers.get(modid).getCategoryComment(helper.category));
                    }
                    helper.initialize(config);
                }
                catch (IllegalAccessException e) {
                    LogHelperBC.error("En error occurred while loading config property: " + helper.name + " for mod: " + modid);
                    Throwables.propagate(e);
                }
            }
            if (config.hasChanged()) {
                config.save();
            }
        }

        modConfigurations.forEach(ModFeatureParser::loadModFeatureConfig);
    }

    public static void addFeatureProperty(String modid, Property prop, String category) {
        modPropertyHelpers.computeIfAbsent(modid, s -> new ArrayList<>()).add(new PropertyHelper(modid, prop, category));
    }

    //endregion

    //region Config GUI stuff

    public static boolean hasConfig(String modid) {
        return modPropertyHelpers.containsKey(modid);
    }

    public static List<ConfigCategory> getModCategories(String modid) {
        return modConfigCategories.getOrDefault(modid, new ArrayList<>());
    }

    public static List<Property> getModProperties(String modid) {
        List<Property> list = new ArrayList<>();
        DataUtils.forEachMatch(modPropertyHelpers.getOrDefault(modid, new ArrayList<>()), prop -> prop.property != null, proop -> list.add(proop.property));
        return list;
    }

    public static void onConfigChanged(String modid) {
        modPropertyHelpers.getOrDefault(modid, new ArrayList<>()).forEach(prop -> {
            if (prop.property.hasChanged()) {
                prop.writeToField();
                modConfigHelpers.get(modid).onConfigChanged(prop.name, prop.category);
            }
        });

        if (modConfigurations.get(modid).hasChanged()) {
            modConfigurations.get(modid).save();
        }
    }

    //endregion

    //region Config client Sync

    public static void writeConfigForSync(MCDataOutput output) {
        int propCount = 0;
        for (List<PropertyHelper> modProps : modPropertyHelpers.values()) {
            propCount += DataUtils.count(modProps, prop -> prop.requiresSync || prop.autoSync);
        }

        output.writeVarInt(propCount); //Just in case the client has an extra mod or something i don't want to assume it has all the same props as the server.
        modPropertyHelpers.forEach((s, props) -> DataUtils.forEachMatch(props, prop -> prop.requiresSync || prop.autoSync, prop -> prop.writeToBytes(output)));
    }

    @SideOnly(Side.CLIENT)
    public static void readConfigForSync(MCDataInput input) {
        int propCount = input.readVarInt();

        Map<PropertyHelper, Object> propsRequireRestart = new HashMap<>();

        for (int i = 0; i < propCount; i++) {
            String propStr = input.readString();
            String propMod = propStr.substring(0, propStr.indexOf(":"));
            String propName = propStr.substring(propStr.indexOf(":") + 1);
            PropertyHelper prop = findProperty(propMod, propName);

            if (prop != null) {
                String[] propValues = {};
                String propValue = "";
                if (prop.isArray) {
                    propValues = new String[input.readVarInt()];
                    for (int c = 0; c < propValues.length; c++) {
                        propValues[c] = input.readString();
                    }
                }
                else {
                    propValue = input.readString();
                }

                if (prop.autoSync) {
                    prop.lockServerValue(propValue, propValues);
                }
                else if (prop.requiresSync) {
                    prop.serverLock = true;
                    if (!prop.property.getString().equals(propValue)) {
                        propsRequireRestart.put(prop, prop.isArray ? propValues : propValue);
                    }
                }
            }
        }

        if (propsRequireRestart.size() > 0) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiIncompatibleConfig(propsRequireRestart));
        }
        connectedToServer = true;
    }

    public static void disconnectFromServer() {
        modPropertyHelpers.forEach((s, props) -> DataUtils.forEachMatch(props, prop -> prop.serverLock, PropertyHelper::unlockServerValue));
        connectedToServer = false;
    }

    public static PropertyHelper findProperty(String modid, String propName) {
        if (modPropertyHelpers.containsKey(modid)) {
            return DataUtils.firstMatch(modPropertyHelpers.get(modid), prop -> prop.name.equals(propName));
        }
        return null;
    }

    public static boolean isPropLocked(String modid, Property property) {
        Configuration config = modConfigurations.get(modid);
        if (config == null) {
            LogHelperBC.bigError("Attempted to access mod config but mod config does not exist! Mod: " + modid);
            return false;
        }

        if (config.getCategory(CATEGORY_BLOCKS).containsKey(property.getName()) || config.getCategory(CATEGORY_ITEMS).containsKey(property.getName())) {
            return connectedToServer;
        }
        else {
            PropertyHelper prop = findProperty(modid, property.getName());
            return prop != null && prop.serverLock;
        }
    }

    public static void acceptServerConfig(Map<PropertyHelper, Object> incompatProps) {
        List<String> mods = new ArrayList<>();
        incompatProps.forEach((prop, o) -> {
            if (prop.isArray && o instanceof String[]) {
                prop.saveServerValue("", (String[]) o);
            }
            else if (!prop.isArray && o instanceof String) {
                prop.saveServerValue((String) o, null);
            }

            if (!mods.contains(prop.modid)) {
                mods.add(prop.modid);
            }
        });

        for (String mod : mods) {
            if (modConfigurations.containsKey(mod)) {
                modConfigurations.get(mod).save();
            }
        }
    }

    //endregion

    public static class PropertyHelper {

        public final String name;
        public final String modid;
        public final String comment;
        public final String category;
        private final Field propField;
        private Object propObj;
        public boolean isArray = false;
        private Property.Type type;
        private final boolean requiresMCRestart;
        private final boolean requiresWorldRestart;
        private final boolean requiresSync;
        private final boolean autoSync;
        private int maxListLength = -1;
        private boolean isListLengthFixed = false;
        public boolean serverLock = false;
        private String clientValue = "";
        private String[] clientValues = {};
        private boolean changedByServer = false;

        public Property property;

        public PropertyHelper(String modid, Property prop, String category) {
            this.modid = modid;
            this.propField = null;
            this.name = prop.getName();
            this.comment = prop.getComment();
            this.category = category;
            this.requiresMCRestart = true;
            this.requiresWorldRestart = true;
            this.requiresSync = true;
            this.autoSync = false;
        }

        public PropertyHelper(String modid, Field propField, ModConfigProperty modProperty) {
            this.modid = modid;
            this.propField = propField;
            this.name = modProperty.name();
            this.comment = modProperty.comment();
            this.category = modProperty.category();
            this.requiresMCRestart = modProperty.requiresMCRestart();
            this.requiresWorldRestart = modProperty.requiresWorldRestart();
            this.requiresSync = modProperty.requiresSync();
            this.autoSync = modProperty.autoSync();
        }

        public void initialize(Configuration config) throws IllegalAccessException {
            this.propObj = propField.get(null);

            if (propObj instanceof Boolean) {
                type = Property.Type.BOOLEAN;
                property = config.get(category, name, (Boolean) propObj, comment);
            }
            else if (propObj instanceof boolean[]) {
                type = Property.Type.BOOLEAN;
                isArray = true;
                property = config.get(category, name, (boolean[]) propObj, comment);
            }
            else if (propObj instanceof Double) {
                type = Property.Type.DOUBLE;
                property = config.get(category, name, (Double) propObj, comment);
            }
            else if (propObj instanceof double[]) {
                type = Property.Type.DOUBLE;
                isArray = true;
                property = config.get(category, name, (double[]) propObj, comment);
            }
            else if (propObj instanceof Integer) {
                type = Property.Type.INTEGER;
                property = config.get(category, name, (Integer) propObj, comment);
            }
            else if (propObj instanceof int[]) {
                type = Property.Type.INTEGER;
                isArray = true;
                property = config.get(category, name, (int[]) propObj, comment);
            }
            else if (propObj instanceof String) {
                type = Property.Type.STRING;
                property = config.get(category, name, (String) propObj, comment);
            }
            else if (propObj instanceof String[]) {
                type = Property.Type.STRING;
                isArray = true;
                property = config.get(category, name, (String[]) propObj, comment);
            }

            if (propField.isAnnotationPresent(ListRestrictions.class) && isArray) {
                ListRestrictions lr = propField.getAnnotation(ListRestrictions.class);
                isListLengthFixed = lr.fixedLength();
                maxListLength = lr.maxLength();
                property.setMaxListLength(maxListLength);
                property.setIsListLengthFixed(isListLengthFixed);
            }

            if (propField.isAnnotationPresent(ValidValues.class)) {
                ValidValues vv = propField.getAnnotation(ValidValues.class);
                LogHelperBC.dev("Value List Detected! " + vv.values().length + " for config " + name);
                property.setValidValues(vv.values());
            }

            if (propField.isAnnotationPresent(MinMax.class)) {
                MinMax mm = propField.getAnnotation(MinMax.class);
                try {
                    if (type == Property.Type.INTEGER) {
                        property.setMinValue(Integer.parseInt(mm.min()));
                        property.setMaxValue(Integer.parseInt(mm.max()));
                    }
                    else if (type == Property.Type.DOUBLE) {
                        property.setMinValue(Double.parseDouble(mm.min()));
                        property.setMaxValue(Double.parseDouble(mm.max()));
                    }
                }
                catch (Exception e) {
                    LogHelperBC.error("An error occurred while parsing the min or max value for property: " + name + " config for mod: " + modid);
                    Throwables.propagate(e);
                }
            }

            property.setRequiresMcRestart(requiresMCRestart);
            property.setRequiresWorldRestart(requiresWorldRestart);

            writeToField();
        }

        public void lockServerValue(String value, String[] values) {
            LogHelperBC.dev("Locking Property: " + name + " to value: " + (isArray ? values : value));
            serverLock = true;

            if (isArray) {
                clientValues = property.getStringList();
            }
            else {
                clientValue = property.getString();
            }

            if (!value.equals(clientValue)) {
                setPropValue(value, values);
                writeToField();
                property.setValue(clientValue);
                ReflectionHelper.setPrivateValue(Property.class, property, false, "changed");
                changedByServer = true;
                if (modConfigHelpers.containsKey(modid)) {
                    modConfigHelpers.get(modid).onConfigChanged(name, category);
                }
            }
        }

        public void unlockServerValue() {
            LogHelperBC.dev("Unlocking Property: " + name);
            serverLock = false;
            if (changedByServer) {
                changedByServer = false;
                setPropValue(clientValue, clientValues);
                writeToField();
                ReflectionHelper.setPrivateValue(Property.class, property, false, "changed");
                if (modConfigHelpers.containsKey(modid)) {
                    modConfigHelpers.get(modid).onConfigChanged(name, category);
                }
            }
        }

        public void saveServerValue(String value, String[] values) {
            serverLock = true;
            if (isArray) {
                clientValues = values;
                property.setValues(values);
            }
            else {
                clientValue = value;
                property.setValue(value);
            }
            writeToField();
        }

        private void setPropValue(String value, String[] values) {
            if (isArray) {
                property.setValues(values);
            }
            else {
                property.setValue(value);
            }
        }

        public void writeToField() {
            if (propField == null) {
                return;
            }
            try {
                switch (type) {
                    case STRING:
                        if (isArray) {
                            propField.set(null, property.getStringList());
                        }
                        else {
                            propField.set(null, property.getString());
                        }
                        break;
                    case INTEGER:
                        if (isArray) {
                            propField.set(null, property.getIntList());
                        }
                        else {
                            propField.set(null, property.getInt());
                        }
                        break;
                    case BOOLEAN:
                        if (isArray) {
                            propField.set(null, property.getBooleanList());
                        }
                        else {
                            propField.set(null, property.getBoolean());
                        }
                        break;
                    case DOUBLE:
                        if (isArray) {
                            propField.set(null, property.getDoubleList());
                        }
                        else {
                            propField.set(null, property.getDouble());
                        }
                }
            }
            catch (Exception e) {
                LogHelperBC.error("An error occurred while trying to set config property: " + name + " for mod: " + modid);
                e.printStackTrace();
            }
        }

        public void writeToBytes(MCDataOutput output) {
            output.writeString(modid + ":" + name);
            if (isArray) {
                output.writeVarInt(property.getStringList().length);
                for (String s : property.getStringList()) {
                    output.writeString(s);
                }
            }
            else {
                output.writeString(property.getString());
            }
        }
    }
}
