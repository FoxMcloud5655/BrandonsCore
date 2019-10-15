package com.brandon3055.brandonscore;

import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.brandonscore.registry.IModConfigHelper;
import com.brandon3055.brandonscore.registry.ModConfigContainer;
import com.brandon3055.brandonscore.registry.ModConfigProperty;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

/**
 * Created by brandon3055 on 15/06/2017.
 */
@ModConfigContainer(modid = "brandonscore")
public class BCConfig implements IModConfigHelper {

    @Override
    public Configuration createConfiguration(FMLPreInitializationEvent event) {
        File configFile = new File(FileHandler.brandon3055Folder, "BrandonsCore.cfg");
        Configuration config = new Configuration(configFile, true);
        if (config.hasKey("Category Booleans", "aBoolean") && configFile.delete()) {
            config = new Configuration(configFile, true); //Delete the old test config file that i accidentally shipped.
        }
        return config;
    }

    @Override
    public String getCategoryComment(String category) {
        return "Comment for category: " + category;
    }

    @Override
    public void onConfigChanged(String name, String category) {
        LogHelperBC.dev("Config Changed! " + name);
    }

    @ModConfigProperty(name = "disableInvasiveConfigGui", category = "Server", comment = "This disables the gui that is shown to clients if a server side config that cant be hot swapped has changed.\nIt is replaced by a chat message that has an option to open the gui")
    public static boolean disableInvasiveGui = false;

    @ModConfigProperty(name = "devLog", category = "Misc", comment = "Enable DEV log output.")
    public static boolean devLog = false;

    @ModConfigProperty(name = "enableTpx", category = "Misc", comment = "Allows you to disable the tpx command.")
    public static boolean enableTpx = !Loader.isModLoaded("mystcraft");

    @ModConfigProperty(name = "darkMode", category = "Client", comment = "Enable gui dark mode.")
    public static boolean darkMode = false;

    @ModConfigProperty(category = "Client Settings", name = "useShaders", comment = "Set this to false if your system can not handle the awesomeness that is shaders! (Warning: Will make cool things look much less cool!)")
    public static boolean useShaders = true;
}
