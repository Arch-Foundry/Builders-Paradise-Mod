package net.archfoundry.buildersparadise.registry

import java.util.function.Consumer
import net.archfoundry.buildersparadise.BuildersParadise
import net.archfoundry.buildersparadise.block.BuilderStationBlock
import net.archfoundry.buildersparadise.block.CastingBasinBlock
import net.archfoundry.buildersparadise.block.CastingTableBlock
import net.archfoundry.buildersparadise.block.CreatorStationBlock
import net.archfoundry.buildersparadise.block.FaucetBlock
import net.archfoundry.buildersparadise.block.ForgeAnvilBlock
import net.archfoundry.buildersparadise.block.ForgeControllerBlock
import net.archfoundry.buildersparadise.block.ForgedBricksBlock
import net.archfoundry.buildersparadise.block.ForgedDrainBlock
import net.archfoundry.buildersparadise.block.ForgedGlassBlock
import net.archfoundry.buildersparadise.block.ForgedTankBlock
import net.archfoundry.buildersparadise.block.PlaceholderBlock
import net.archfoundry.buildersparadise.block.SmelteryBlock
import net.archfoundry.buildersparadise.block.entity.BuilderStationBlockEntity
import net.archfoundry.buildersparadise.block.entity.CastingBasinBlockEntity
import net.archfoundry.buildersparadise.block.entity.CastingTableBlockEntity
import net.archfoundry.buildersparadise.block.entity.CreatorStationBlockEntity
import net.archfoundry.buildersparadise.block.entity.FaucetBlockEntity
import net.archfoundry.buildersparadise.block.entity.ForgeAnvilBlockEntity
import net.archfoundry.buildersparadise.block.entity.ForgeControllerBlockEntity
import net.archfoundry.buildersparadise.block.entity.ForgedDrainBlockEntity
import net.archfoundry.buildersparadise.block.entity.ForgedTankBlockEntity
import net.archfoundry.buildersparadise.block.entity.PlaceholderBlockEntity
import net.archfoundry.buildersparadise.block.entity.SmelteryBlockEntity
import net.archfoundry.buildersparadise.item.BlueprintItem
import net.archfoundry.buildersparadise.item.CastItem
import net.archfoundry.buildersparadise.item.CustomPickaxeItem
import net.archfoundry.buildersparadise.item.HammerItem
import net.archfoundry.buildersparadise.item.ToolPartItem
import net.archfoundry.buildersparadise.menu.BuilderStationMenu
import net.archfoundry.buildersparadise.menu.SmelteryMenu
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.BucketItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.FlowingFluid
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions
import net.minecraftforge.common.SoundActions
import net.minecraftforge.common.extensions.IForgeMenuType
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fluids.FluidType
import net.minecraftforge.fluids.ForgeFlowingFluid
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject

object RegistryModule {
        val ITEMS: DeferredRegister<Item> =
                DeferredRegister.create(ForgeRegistries.ITEMS, BuildersParadise.MOD_ID)
        val TABS: DeferredRegister<CreativeModeTab> =
                DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BuildersParadise.MOD_ID)
        val FLUID_TYPES: DeferredRegister<FluidType> =
                DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, BuildersParadise.MOD_ID)
        val FLUIDS: DeferredRegister<Fluid> =
                DeferredRegister.create(ForgeRegistries.FLUIDS, BuildersParadise.MOD_ID)

        // Blocks
        val BLOCKS: DeferredRegister<Block> =
                DeferredRegister.create(ForgeRegistries.BLOCKS, BuildersParadise.MOD_ID)

        val PLACEHOLDER_BLOCK: RegistryObject<Block> =
                BLOCKS.register("placeholder_block") {
                        PlaceholderBlock(
                                BlockBehaviour.Properties.of()
                                        .strength(1.5f)
                                        .requiresCorrectToolForDrops()
                        )
                }
        val BUILDER_STATION: RegistryObject<Block> =
                BLOCKS.register("builder_station") {
                        BuilderStationBlock(
                                BlockBehaviour.Properties.of()
                                        .strength(2.5f)
                                        .requiresCorrectToolForDrops()
                                        .noOcclusion()
                        )
                }

        val MOLTEN_STONE_BLOCK: RegistryObject<LiquidBlock> =
                BLOCKS.register("molten_stone_block") {
                        LiquidBlock(
                                MOLTEN_STONE_SOURCE,
                                BlockBehaviour.Properties.of()
                                        .noCollission()
                                        .strength(100.0f)
                                        .noLootTable()
                                        .replaceable()
                        )
                }
        val MOLTEN_GLASS_BLOCK: RegistryObject<LiquidBlock> =
                BLOCKS.register("molten_glass_block") {
                        LiquidBlock(
                                MOLTEN_GLASS_SOURCE,
                                BlockBehaviour.Properties.of()
                                        .noCollission()
                                        .strength(100.0f)
                                        .noLootTable()
                                        .replaceable()
                        )
                }
        val SMELTERY_BLOCK: RegistryObject<Block> =
                BLOCKS.register("smeltery_block") {
                        SmelteryBlock(
                                BlockBehaviour.Properties.of()
                                        .strength(3.5f)
                                        .requiresCorrectToolForDrops()
                        )
                }
        val CASTING_TABLE_BLOCK: RegistryObject<Block> =
                BLOCKS.register("casting_table") {
                        CastingTableBlock(
                                BlockBehaviour.Properties.of()
                                        .strength(3.5f)
                                        .requiresCorrectToolForDrops()
                                        .noOcclusion()
                        )
                }
        val CASTING_BASIN_BLOCK: RegistryObject<Block> =
                BLOCKS.register("casting_basin") {
                        CastingBasinBlock(
                                BlockBehaviour.Properties.of()
                                        .strength(3.5f)
                                        .requiresCorrectToolForDrops()
                                        .noOcclusion()
                        )
                }
        val FAUCET_BLOCK: RegistryObject<Block> =
                BLOCKS.register("faucet") {
                        FaucetBlock(
                                BlockBehaviour.Properties.of()
                                        .strength(2.0f)
                                        .requiresCorrectToolForDrops()
                                        .noOcclusion()
                        )
                }
        val FORGE_ANVIL_BLOCK: RegistryObject<Block> =
                BLOCKS.register("forge_anvil") {
                        ForgeAnvilBlock(
                                BlockBehaviour.Properties.copy(
                                        net.minecraft.world.level.block.Blocks.ANVIL
                                )
                        )
                }
        val CREATOR_STATION_BLOCK: RegistryObject<Block> =
                BLOCKS.register("creator_station") {
                        CreatorStationBlock(
                                BlockBehaviour.Properties.of()
                                        .strength(2.5f)
                                        .requiresCorrectToolForDrops()
                                        .noOcclusion()
                        )
                }

        // The Forge Blocks
        val FORGED_BRICKS_BLOCK: RegistryObject<Block> =
                BLOCKS.register("forged_bricks") {
                        ForgedBricksBlock(
                                BlockBehaviour.Properties.of()
                                        .strength(4.0f)
                                        .requiresCorrectToolForDrops()
                                        .sound(SoundType.STONE)
                        )
                }
        val FORGED_GLASS_BLOCK: RegistryObject<Block> =
                BLOCKS.register("forged_glass") {
                        ForgedGlassBlock(
                                BlockBehaviour.Properties.of()
                                        .strength(3.0f)
                                        .requiresCorrectToolForDrops()
                                        .sound(SoundType.GLASS)
                                        .noOcclusion()
                        )
                }
        val FORGE_CONTROLLER_BLOCK: RegistryObject<Block> =
                BLOCKS.register("forge_controller") {
                        ForgeControllerBlock(
                                BlockBehaviour.Properties.of()
                                        .strength(5.0f)
                                        .requiresCorrectToolForDrops()
                                        .sound(SoundType.METAL)
                        )
                }
        val FORGED_TANK_BLOCK: RegistryObject<Block> =
                BLOCKS.register("forged_tank") {
                        ForgedTankBlock(
                                BlockBehaviour.Properties.of()
                                        .strength(4.0f)
                                        .requiresCorrectToolForDrops()
                                        .sound(SoundType.GLASS)
                                        .noOcclusion()
                        )
                }
        val FORGED_DRAIN_BLOCK: RegistryObject<Block> =
                BLOCKS.register("forged_drain") {
                        ForgedDrainBlock(
                                BlockBehaviour.Properties.of()
                                        .strength(4.0f)
                                        .requiresCorrectToolForDrops()
                                        .sound(SoundType.METAL)
                        )
                }

        // Block Entities
        val BLOCK_ENTITIES: DeferredRegister<BlockEntityType<*>> =
                DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BuildersParadise.MOD_ID)

        val BUILDER_STATION_BE: RegistryObject<BlockEntityType<BuilderStationBlockEntity>> =
                BLOCK_ENTITIES.register("builder_station") {
                        BlockEntityType.Builder.of(
                                        ::BuilderStationBlockEntity,
                                        BUILDER_STATION.get()
                                )
                                .build(null)
                }

        val PLACEHOLDER_BLOCK_BE: RegistryObject<BlockEntityType<PlaceholderBlockEntity>> =
                BLOCK_ENTITIES.register("placeholder_block") {
                        BlockEntityType.Builder.of(
                                        ::PlaceholderBlockEntity,
                                        PLACEHOLDER_BLOCK.get()
                                )
                                .build(null)
                }

        val SMELTERY_BE: RegistryObject<BlockEntityType<SmelteryBlockEntity>> =
                BLOCK_ENTITIES.register("smeltery_block") {
                        BlockEntityType.Builder.of(::SmelteryBlockEntity, SMELTERY_BLOCK.get())
                                .build(null)
                }

        val CASTING_TABLE_BE: RegistryObject<BlockEntityType<CastingTableBlockEntity>> =
                BLOCK_ENTITIES.register("casting_table") {
                        BlockEntityType.Builder.of(
                                        ::CastingTableBlockEntity,
                                        CASTING_TABLE_BLOCK.get()
                                )
                                .build(null)
                }
        val CASTING_BASIN_BE: RegistryObject<BlockEntityType<CastingBasinBlockEntity>> =
                BLOCK_ENTITIES.register("casting_basin") {
                        BlockEntityType.Builder.of(
                                        ::CastingBasinBlockEntity,
                                        CASTING_BASIN_BLOCK.get()
                                )
                                .build(null)
                }
        val FAUCET_BE: RegistryObject<BlockEntityType<FaucetBlockEntity>> =
                BLOCK_ENTITIES.register("faucet") {
                        BlockEntityType.Builder.of(::FaucetBlockEntity, FAUCET_BLOCK.get())
                                .build(null)
                }
        val FORGE_ANVIL_BE: RegistryObject<BlockEntityType<ForgeAnvilBlockEntity>> =
                BLOCK_ENTITIES.register("forge_anvil") {
                        BlockEntityType.Builder.of(::ForgeAnvilBlockEntity, FORGE_ANVIL_BLOCK.get())
                                .build(null)
                }
        val CREATOR_STATION_NEW_BE: RegistryObject<BlockEntityType<CreatorStationBlockEntity>> =
                BLOCK_ENTITIES.register("creator_station_new") {
                        BlockEntityType.Builder.of(
                                        ::CreatorStationBlockEntity,
                                        CREATOR_STATION_BLOCK.get()
                                )
                                .build(null)
                }

        val FORGE_CONTROLLER_BE: RegistryObject<BlockEntityType<ForgeControllerBlockEntity>> =
                BLOCK_ENTITIES.register("forge_controller") {
                        BlockEntityType.Builder.of(
                                        ::ForgeControllerBlockEntity,
                                        FORGE_CONTROLLER_BLOCK.get()
                                )
                                .build(null)
                }
        val FORGED_TANK_BE: RegistryObject<BlockEntityType<ForgedTankBlockEntity>> =
                BLOCK_ENTITIES.register("forged_tank") {
                        BlockEntityType.Builder.of(::ForgedTankBlockEntity, FORGED_TANK_BLOCK.get())
                                .build(null)
                }
        val FORGED_DRAIN_BE: RegistryObject<BlockEntityType<ForgedDrainBlockEntity>> =
                BLOCK_ENTITIES.register("forged_drain") {
                        BlockEntityType.Builder.of(
                                        ::ForgedDrainBlockEntity,
                                        FORGED_DRAIN_BLOCK.get()
                                )
                                .build(null)
                }

        // Menus
        val MENUS: DeferredRegister<MenuType<*>> =
                DeferredRegister.create(ForgeRegistries.MENU_TYPES, BuildersParadise.MOD_ID)

        val BUILDER_STATION_MENU: RegistryObject<MenuType<BuilderStationMenu>> =
                MENUS.register("builder_station") {
                        IForgeMenuType.create { windowId, inv, data ->
                                BuilderStationMenu(windowId, inv, data)
                        }
                }

        val SMELTERY_MENU: RegistryObject<MenuType<SmelteryMenu>> =
                MENUS.register("smeltery_block") {
                        IForgeMenuType.create { windowId, inv, data ->
                                SmelteryMenu(windowId, inv, data)
                        }
                }

        // Fluids
        // Molten Stone
        val MOLTEN_STONE_TYPE: RegistryObject<FluidType> =
                FLUID_TYPES.register("molten_stone") {
                        object :
                                FluidType(
                                        Properties.create()
                                                .lightLevel(15)
                                                .density(3000)
                                                .viscosity(6000)
                                                .temperature(1300)
                                                .sound(
                                                        SoundActions.BUCKET_FILL,
                                                        SoundEvents.BUCKET_FILL_LAVA
                                                )
                                                .sound(
                                                        SoundActions.BUCKET_EMPTY,
                                                        SoundEvents.BUCKET_EMPTY_LAVA
                                                )
                                ) {
                                override fun initializeClient(
                                        consumer: Consumer<IClientFluidTypeExtensions>
                                ) {
                                        consumer.accept(
                                                object : IClientFluidTypeExtensions {
                                                        override fun getStillTexture():
                                                                net.minecraft.resources.ResourceLocation {
                                                                return net.minecraft.resources
                                                                        .ResourceLocation(
                                                                                "block/lava_still"
                                                                        )
                                                        }

                                                        override fun getFlowingTexture():
                                                                net.minecraft.resources.ResourceLocation {
                                                                return net.minecraft.resources
                                                                        .ResourceLocation(
                                                                                "block/lava_flow"
                                                                        )
                                                        }

                                                        override fun getTintColor(): Int {
                                                                return 0xFFFF4500.toLong().toInt()
                                                        }
                                                }
                                        )
                                }
                        }
                }

        val MOLTEN_STONE_PROPERTIES: ForgeFlowingFluid.Properties
                get() =
                        ForgeFlowingFluid.Properties(
                                        MOLTEN_STONE_TYPE,
                                        MOLTEN_STONE_SOURCE,
                                        MOLTEN_STONE_FLOWING
                                )
                                .bucket(MOLTEN_STONE_BUCKET)
                                .block(MOLTEN_STONE_BLOCK)

        val MOLTEN_STONE_SOURCE: RegistryObject<FlowingFluid> =
                FLUIDS.register("molten_stone_source") {
                        ForgeFlowingFluid.Source(MOLTEN_STONE_PROPERTIES)
                }

        val MOLTEN_STONE_FLOWING: RegistryObject<FlowingFluid> =
                FLUIDS.register("molten_stone_flowing") {
                        ForgeFlowingFluid.Flowing(MOLTEN_STONE_PROPERTIES)
                }

        // Molten Glass
        val MOLTEN_GLASS_TYPE: RegistryObject<FluidType> =
                FLUID_TYPES.register("molten_glass") {
                        object :
                                FluidType(
                                        Properties.create()
                                                .lightLevel(10)
                                                .density(2500)
                                                .viscosity(4000)
                                                .temperature(1000)
                                                .sound(
                                                        SoundActions.BUCKET_FILL,
                                                        SoundEvents.BUCKET_FILL_LAVA
                                                )
                                                .sound(
                                                        SoundActions.BUCKET_EMPTY,
                                                        SoundEvents.BUCKET_EMPTY_LAVA
                                                )
                                ) {
                                override fun initializeClient(
                                        consumer: Consumer<IClientFluidTypeExtensions>
                                ) {
                                        consumer.accept(
                                                object : IClientFluidTypeExtensions {
                                                        override fun getStillTexture():
                                                                net.minecraft.resources.ResourceLocation {
                                                                return net.minecraft.resources
                                                                        .ResourceLocation(
                                                                                "block/water_still"
                                                                        )
                                                        }
                                                        override fun getFlowingTexture():
                                                                net.minecraft.resources.ResourceLocation {
                                                                return net.minecraft.resources
                                                                        .ResourceLocation(
                                                                                "block/water_flow"
                                                                        )
                                                        }
                                                        override fun getTintColor(): Int {
                                                                return 0xFF88CCEE
                                                                        .toLong()
                                                                        .toInt() // Light Blue
                                                        }
                                                }
                                        )
                                }
                        }
                }

        val MOLTEN_GLASS_PROPERTIES: ForgeFlowingFluid.Properties
                get() =
                        ForgeFlowingFluid.Properties(
                                        MOLTEN_GLASS_TYPE,
                                        MOLTEN_GLASS_SOURCE,
                                        MOLTEN_GLASS_FLOWING
                                )
                                .bucket(MOLTEN_GLASS_BUCKET)
                                .block(MOLTEN_GLASS_BLOCK)

        val MOLTEN_GLASS_SOURCE: RegistryObject<FlowingFluid> =
                FLUIDS.register("molten_glass_source") {
                        ForgeFlowingFluid.Source(MOLTEN_GLASS_PROPERTIES)
                }

        val MOLTEN_GLASS_FLOWING: RegistryObject<FlowingFluid> =
                FLUIDS.register("molten_glass_flowing") {
                        ForgeFlowingFluid.Flowing(MOLTEN_GLASS_PROPERTIES)
                }

        // Items

        val BLUEPRINT_ITEM: RegistryObject<Item> =
                ITEMS.register("blueprint") { BlueprintItem(Item.Properties().stacksTo(1)) }
        val PLACEHOLDER_BLOCK_ITEM: RegistryObject<Item> =
                ITEMS.register("placeholder_block") {
                        BlockItem(PLACEHOLDER_BLOCK.get(), Item.Properties())
                }
        val BUILDER_STATION_ITEM: RegistryObject<Item> =
                ITEMS.register("builder_station") {
                        BlockItem(BUILDER_STATION.get(), Item.Properties())
                }

        val MOLTEN_STONE_BUCKET: RegistryObject<Item> =
                ITEMS.register("molten_stone_bucket") {
                        BucketItem(
                                MOLTEN_STONE_SOURCE,
                                Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                        )
                }
        val MOLTEN_GLASS_BUCKET: RegistryObject<Item> =
                ITEMS.register("molten_glass_bucket") {
                        BucketItem(
                                MOLTEN_GLASS_SOURCE,
                                Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                        )
                }
        val SMELTERY_ITEM: RegistryObject<Item> =
                ITEMS.register("smeltery_block") {
                        BlockItem(SMELTERY_BLOCK.get(), Item.Properties())
                }
        val CASTING_TABLE_ITEM: RegistryObject<Item> =
                ITEMS.register("casting_table") {
                        BlockItem(CASTING_TABLE_BLOCK.get(), Item.Properties())
                }
        val CASTING_BASIN_ITEM: RegistryObject<Item> =
                ITEMS.register("casting_basin") {
                        BlockItem(CASTING_BASIN_BLOCK.get(), Item.Properties())
                }
        val FAUCET_ITEM: RegistryObject<Item> =
                ITEMS.register("faucet") { BlockItem(FAUCET_BLOCK.get(), Item.Properties()) }
        val FORGE_ANVIL_ITEM: RegistryObject<Item> =
                ITEMS.register("forge_anvil") {
                        BlockItem(FORGE_ANVIL_BLOCK.get(), Item.Properties())
                }
        val CREATOR_STATION_NEW_ITEM: RegistryObject<Item> =
                ITEMS.register("creator_station_new") {
                        BlockItem(CREATOR_STATION_BLOCK.get(), Item.Properties())
                }

        val FORGED_BRICKS_ITEM: RegistryObject<Item> =
                ITEMS.register("forged_bricks") {
                        BlockItem(FORGED_BRICKS_BLOCK.get(), Item.Properties())
                }
        val FORGED_GLASS_ITEM: RegistryObject<Item> =
                ITEMS.register("forged_glass") {
                        BlockItem(FORGED_GLASS_BLOCK.get(), Item.Properties())
                }
        val FORGE_CONTROLLER_ITEM: RegistryObject<Item> =
                ITEMS.register("forge_controller") {
                        BlockItem(FORGE_CONTROLLER_BLOCK.get(), Item.Properties())
                }
        val FORGED_TANK_ITEM: RegistryObject<Item> =
                ITEMS.register("forged_tank") {
                        BlockItem(FORGED_TANK_BLOCK.get(), Item.Properties())
                }
        val FORGED_DRAIN_ITEM: RegistryObject<Item> =
                ITEMS.register("forged_drain") {
                        BlockItem(FORGED_DRAIN_BLOCK.get(), Item.Properties())
                }

        // Casts
        // Casts
        val INGOT_CAST: RegistryObject<Item> =
                ITEMS.register("ingot_cast") { CastItem(Item.Properties(), 144) }
        val PICKAXE_HEAD_CAST: RegistryObject<Item> =
                ITEMS.register("pickaxe_head_cast") { CastItem(Item.Properties(), 144) }
        val TOOL_HANDLE_CAST: RegistryObject<Item> =
                ITEMS.register("tool_handle_cast") { CastItem(Item.Properties(), 144) }
        val TOOL_BINDING_CAST: RegistryObject<Item> =
                ITEMS.register("tool_binding_cast") { CastItem(Item.Properties(), 144) }
        val AXE_HEAD_CAST: RegistryObject<Item> =
                ITEMS.register("axe_head_cast") { CastItem(Item.Properties(), 144) }
        val SHOVEL_HEAD_CAST: RegistryObject<Item> =
                ITEMS.register("shovel_head_cast") { CastItem(Item.Properties(), 144) }
        val SWORD_BLADE_CAST: RegistryObject<Item> =
                ITEMS.register("sword_blade_cast") { CastItem(Item.Properties(), 144) }
        val PLATE_CAST: RegistryObject<Item> =
                ITEMS.register("plate_cast") { CastItem(Item.Properties(), 144) }
        val GEAR_CAST: RegistryObject<Item> =
                ITEMS.register("gear_cast") { CastItem(Item.Properties(), 576) }

        // Tools & Parts
        val HAMMER_ITEM: RegistryObject<Item> =
                ITEMS.register("hammer") { HammerItem(Item.Properties().durability(100)) }

        val CUSTOM_PICKAXE_ITEM: RegistryObject<Item> =
                ITEMS.register("custom_pickaxe") {
                        CustomPickaxeItem(Item.Properties().durability(500))
                }

        // Parts - Head
        val RAW_PICKAXE_HEAD: RegistryObject<Item> =
                ITEMS.register("raw_pickaxe_head") {
                        ToolPartItem(Item.Properties(), ToolPartItem.PartType.HEAD, false)
                }
        val PICKAXE_HEAD: RegistryObject<Item> =
                ITEMS.register("pickaxe_head") {
                        ToolPartItem(Item.Properties(), ToolPartItem.PartType.HEAD, true)
                }

        // Parts - Handle
        val RAW_TOOL_HANDLE: RegistryObject<Item> =
                ITEMS.register("raw_tool_handle") {
                        ToolPartItem(Item.Properties(), ToolPartItem.PartType.HANDLE, false)
                }
        val TOOL_HANDLE: RegistryObject<Item> =
                ITEMS.register("tool_handle") {
                        ToolPartItem(Item.Properties(), ToolPartItem.PartType.HANDLE, true)
                }

        // Parts - Binding
        val RAW_TOOL_BINDING: RegistryObject<Item> =
                ITEMS.register("raw_tool_binding") {
                        ToolPartItem(Item.Properties(), ToolPartItem.PartType.BINDING, false)
                }
        val TOOL_BINDING: RegistryObject<Item> =
                ITEMS.register("tool_binding") {
                        ToolPartItem(Item.Properties(), ToolPartItem.PartType.BINDING, true)
                }

        // Parts - Axe Head
        val RAW_AXE_HEAD: RegistryObject<Item> =
                ITEMS.register("raw_axe_head") {
                        ToolPartItem(Item.Properties(), ToolPartItem.PartType.AXE_HEAD, false)
                }
        val AXE_HEAD: RegistryObject<Item> =
                ITEMS.register("axe_head") {
                        ToolPartItem(Item.Properties(), ToolPartItem.PartType.AXE_HEAD, true)
                }

        // Parts - Shovel Head
        val RAW_SHOVEL_HEAD: RegistryObject<Item> =
                ITEMS.register("raw_shovel_head") {
                        ToolPartItem(Item.Properties(), ToolPartItem.PartType.SHOVEL_HEAD, false)
                }
        val SHOVEL_HEAD: RegistryObject<Item> =
                ITEMS.register("shovel_head") {
                        ToolPartItem(Item.Properties(), ToolPartItem.PartType.SHOVEL_HEAD, true)
                }

        // Parts - Sword Blade
        val RAW_SWORD_BLADE: RegistryObject<Item> =
                ITEMS.register("raw_sword_blade") {
                        ToolPartItem(Item.Properties(), ToolPartItem.PartType.SWORD_BLADE, false)
                }
        val SWORD_BLADE: RegistryObject<Item> =
                ITEMS.register("sword_blade") {
                        ToolPartItem(Item.Properties(), ToolPartItem.PartType.SWORD_BLADE, true)
                }

        // Creative Tab
        val BUILDERS_PARADISE_TAB: RegistryObject<CreativeModeTab> =
                TABS.register("builders_paradise_tab") {
                        CreativeModeTab.builder()
                                .title(Component.translatable("itemGroup.buildersparadise"))
                                .icon { ItemStack(BLUEPRINT_ITEM.get()) }
                                .displayItems { parameters, output ->
                                        output.accept(BLUEPRINT_ITEM.get())
                                        output.accept(PLACEHOLDER_BLOCK_ITEM.get())
                                        output.accept(BUILDER_STATION_ITEM.get())
                                        output.accept(MOLTEN_STONE_BUCKET.get())
                                        output.accept(MOLTEN_GLASS_BUCKET.get())
                                        output.accept(SMELTERY_ITEM.get())
                                        output.accept(CASTING_TABLE_ITEM.get())
                                        output.accept(CASTING_BASIN_ITEM.get())
                                        output.accept(FAUCET_ITEM.get())
                                        output.accept(INGOT_CAST.get())
                                        output.accept(PICKAXE_HEAD_CAST.get())
                                        output.accept(TOOL_HANDLE_CAST.get())
                                        output.accept(TOOL_BINDING_CAST.get())
                                        output.accept(AXE_HEAD_CAST.get())
                                        output.accept(SHOVEL_HEAD_CAST.get())
                                        output.accept(SWORD_BLADE_CAST.get())
                                        output.accept(PLATE_CAST.get())
                                        output.accept(GEAR_CAST.get())

                                        output.accept(HAMMER_ITEM.get())
                                        output.accept(CUSTOM_PICKAXE_ITEM.get())
                                        output.accept(RAW_PICKAXE_HEAD.get())
                                        output.accept(PICKAXE_HEAD.get())
                                        output.accept(RAW_TOOL_HANDLE.get())
                                        output.accept(TOOL_HANDLE.get())
                                        output.accept(RAW_TOOL_BINDING.get())

                                        output.accept(TOOL_BINDING.get())
                                        output.accept(RAW_AXE_HEAD.get())
                                        output.accept(AXE_HEAD.get())
                                        output.accept(RAW_SHOVEL_HEAD.get())
                                        output.accept(SHOVEL_HEAD.get())
                                        output.accept(RAW_SWORD_BLADE.get())
                                        output.accept(SWORD_BLADE.get())

                                        output.accept(FORGE_ANVIL_ITEM.get())
                                        output.accept(CREATOR_STATION_NEW_ITEM.get())

                                        output.accept(FORGED_BRICKS_ITEM.get())
                                        output.accept(FORGED_GLASS_ITEM.get())
                                        output.accept(FORGE_CONTROLLER_ITEM.get())
                                        output.accept(FORGED_TANK_ITEM.get())
                                        output.accept(FORGED_DRAIN_ITEM.get())
                                }
                                .build()
                }

        fun register(modBus: IEventBus) {
                BLOCKS.register(modBus)
                BLOCK_ENTITIES.register(modBus)
                MENUS.register(modBus)
                ITEMS.register(modBus)
                TABS.register(modBus)
                FLUID_TYPES.register(modBus)
                FLUIDS.register(modBus)
        }
}
