package com.playertag.client.mixin;

import com.playertag.client.config.TagConfig;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin
        extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public PlayerEntityRendererMixin(EntityRendererFactory.Context ctx,
                                     PlayerEntityModel<AbstractClientPlayerEntity> model,
                                     float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(AbstractClientPlayerEntity player, float yaw, float tickDelta,
                          MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                          int light, CallbackInfo ci) {
        if (!TagConfig.get().enabled) return;

        String name = player.getName().getString();
        TagConfig.PlayerCategory cat = TagConfig.get().getCategory(name);

        if (cat == TagConfig.PlayerCategory.NONE) return;

        // Apply outline glow effect using the outline flag
        if (TagConfig.get().showGlow) {
            if (cat == TagConfig.PlayerCategory.FRIEND) {
                player.setGlowing(true);
                // We override the team color via display name color below
            } else {
                player.setGlowing(true);
            }
        }
    }

    @Override
    public Text getDisplayName(AbstractClientPlayerEntity entity) {
        if (!TagConfig.get().enabled) return super.getDisplayName(entity);

        String name = entity.getName().getString();
        TagConfig.PlayerCategory cat = TagConfig.get().getCategory(name);

        if (TagConfig.get().showNametagColor) {
            if (cat == TagConfig.PlayerCategory.FRIEND) {
                return Text.literal("§a" + name);
            } else if (cat == TagConfig.PlayerCategory.ENEMY) {
                return Text.literal("§c" + name);
            }
        }
        return super.getDisplayName(entity);
    }
}
