package com.binaris.wizardry.client;

import com.binaris.wizardry.api.content.spell.SpellAction;
import com.binaris.wizardry.client.effect.ArcaneLockRender;
import com.binaris.wizardry.client.effect.ContainmentFieldRender;
import com.binaris.wizardry.client.model.GlowingOverlayModel;
import com.binaris.wizardry.core.config.ConfigManager;
import com.binaris.wizardry.network.EBFabricClientNetwork;
import com.binaris.wizardry.setup.registries.EBBlocks;
import com.binaris.wizardry.setup.registries.client.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public final class WizardryFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WizardryClientMod.init();
        EBFabricClientNetwork.registerS2CMessages();
        EBClientEventHelper.register();

        SpellAction.register();
        EBRenderers.registerRenderers();
        EBRenderers.getRenderers().forEach((entity, renderer) -> EntityRendererRegistry.register(entity.get(), (EntityRendererProvider<Entity>) renderer));

        EBParticleProviders.registerProvider((p, f) -> {
            var reg = ParticleFactoryRegistry.getInstance();
            reg.register(p.get(), f::apply);
        });

        HudRenderCallback.EVENT.register(((guiGraphics, delta) -> SpellGUIDisplay.draw(guiGraphics, guiGraphics.pose(), delta)));

        EBRenderers.createEntityLayers((layer, supplier) -> EntityModelLayerRegistry.registerModelLayer(layer, supplier::get));
        EBArmorRenderFabric.load();

        EBItemProperties.register();
        KeyBindingHelper.registerKeyBinding(EBKeyBinding.NEXT_SPELL);
        KeyBindingHelper.registerKeyBinding(EBKeyBinding.PREVIOUS_SPELL);
        for (int i = 0; i < EBKeyBinding.SPELL_QUICK_ACCESS.length; i++) {
            KeyBindingHelper.registerKeyBinding(EBKeyBinding.SPELL_QUICK_ACCESS[i]);
        }

        EBMenuScreens.init();
        EBMenuScreens.register((menuType, screenFactory) -> MenuScreens.register(menuType, screenFactory::create));

        EBBlockEntityRenderers.init();
        EBBlockEntityRenderers.register(BlockEntityRenderers::register);

        BlockRenderLayerMap.INSTANCE.putBlock(EBBlocks.VANISHING_COBWEB.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(EBBlocks.CRYSTAL_FLOWER.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(EBBlocks.POTTED_CRYSTAL_FLOWER.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(EBBlocks.OAK_LECTERN.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(EBBlocks.SPRUCE_LECTERN.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(EBBlocks.BIRCH_LECTERN.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(EBBlocks.JUNGLE_LECTERN.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(EBBlocks.ACACIA_LECTERN.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(EBBlocks.DARK_OAK_LECTERN.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(EBBlocks.IMBUEMENT_ALTAR.get(), RenderType.cutoutMipped());

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(),
                EBBlocks.FIRE_RUNESTONE_PEDESTAL.get(),
                EBBlocks.ICE_RUNESTONE_PEDESTAL.get(),
                EBBlocks.LIGHTNING_RUNESTONE_PEDESTAL.get(),
                EBBlocks.NECROMANCY_RUNESTONE_PEDESTAL.get(),
                EBBlocks.EARTH_RUNESTONE_PEDESTAL.get(),
                EBBlocks.SORCERY_RUNESTONE_PEDESTAL.get(),
                EBBlocks.HEALING_RUNESTONE_PEDESTAL.get()
        );
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(),
                EBBlocks.FIRE_RUNESTONE.get(),
                EBBlocks.ICE_RUNESTONE.get(),
                EBBlocks.LIGHTNING_RUNESTONE.get(),
                EBBlocks.NECROMANCY_RUNESTONE.get(),
                EBBlocks.EARTH_RUNESTONE.get(),
                EBBlocks.SORCERY_RUNESTONE.get(),
                EBBlocks.HEALING_RUNESTONE.get()
        );

        WorldRenderEvents.AFTER_TRANSLUCENT.register(this::renderContainmentField);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ConfigManager.restoreLocalConfigs();
        });

        ModelLoadingPlugin.register(ctx -> ctx.modifyModelAfterBake().register(
                (model, context) -> {
                    ResourceLocation location = context.id();
                    if(location.getPath().contains("runestone") ||
                            location.getPath().contains("runestone_pedestal") ||
                            location.getPath().contains("imbuement_altar")) {
                        return new GlowingOverlayModel(model, "overlay");
                    } else if (location.getPath().contains("spectral_block")) {
                        return new GlowingOverlayModel(model, "spectral_block");
                    }
                    return model;
        }));
    }

    private void renderContainmentField(WorldRenderContext ctx) {
        ContainmentFieldRender.render(ctx.camera(), ctx.matrixStack(), ctx.tickDelta());
        ArcaneLockRender.render(ctx.camera(), ctx.matrixStack(), ctx.tickDelta());
    }
}
