package uk.co.craig.cobbleverse.mixin;

import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.input.GamepadInputs;
import dev.isxander.controlify.bindings.output.KeyMappingEmulationOutput;
import net.minecraft.class_304;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = KeyMappingEmulationOutput.class, remap = false)
public abstract class KeyMappingEmulationOutputMixin {
    @Shadow(remap = false) private ControllerEntity controller;
    @Shadow(remap = false) private class_304 keyMapping;

    @Inject(method = "push", at = @At("HEAD"), cancellable = true, remap = false)
    private void cobbleverse$suppressEmulatedKeysDuringModifier(CallbackInfo callback) {
        if ("key.sprint".equals(keyMapping.method_1431())) return;

        boolean leftStickHeld = controller.input()
                .map(input -> input.stateNow().isButtonDown(GamepadInputs.LEFT_STICK_BUTTON))
                .orElse(false);
        if (leftStickHeld) {
            keyMapping.method_23481(false);
            callback.cancel();
        }
    }
}
