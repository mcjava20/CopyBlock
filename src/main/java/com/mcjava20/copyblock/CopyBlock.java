package com.mcjava20.copyblock;

// import com.mcjava20.copyblock.command.CopyBlockCommand; //删掉这行
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
// import net.neoforged.neoforge.event.RegisterCommandsEvent; //删掉

@Mod(CopyBlock.MODID)
public class CopyBlock {
    public static final String MODID = "copyblock";

    public CopyBlock(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModItems.CREATIVE_TABS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        // ========== 删除命令注册 ==========
        // IEventBus gameBus = NeoForge.EVENT_BUS;
        // gameBus.addListener(CopyBlock::onRegisterCommands);
    }

    // private static void onRegisterCommands(RegisterCommandsEvent event) {
    //     CopyBlockCommand.register(event.getDispatcher());
    // }
}
