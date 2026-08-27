package uk.co.craig.cobbleverse;

import com.cobblemon.mod.common.CobblemonNetwork;
import com.cobblemon.mod.common.net.messages.server.SendOutPokemonPacket;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.activestate.ActivePokemonState;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.api.entrypoint.InitContext;
import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.controller.input.GamepadInputs;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import org.lwjgl.glfw.GLFW;

public final class PartyWideActionClient implements ClientModInitializer, ControlifyEntrypoint {
    private static final String NAMESPACE = "cobbleverse_party_wide_action";
    private static final class_2960 ACTION_ID = class_2960.method_60654(NAMESPACE + ":toggle");
    private static class_304 key;
    private static boolean chordWasDown;

    @Override
    public void onInitializeClient() {
        key = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.cobbleverse_party_wide_action.toggle",
                class_3675.class_307.field_1668,
                GLFW.GLFW_KEY_UNKNOWN,
                "key.categories.cobbleverse_party_wide_action"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (key.method_1434()) {
                toggleParty(client);
            }
        });
        ControlifyEvents.ACTIVE_CONTROLLER_TICKED.register(update -> {
            var input = update.controller().input().orElse(null);
            boolean chordDown = input != null
                    && input.stateNow().isButtonDown(GamepadInputs.LEFT_STICK_BUTTON)
                    && input.stateNow().isButtonDown(GamepadInputs.EAST_BUTTON);
            if (chordDown && !chordWasDown) {
                toggleParty(net.minecraft.class_310.method_1551());
            }
            chordWasDown = chordDown;
        });
    }

    @Override
    public void onControlifyInit(InitContext context) {
        ControlifyBindApi.get().registerBinding(builder -> builder
                .id(ACTION_ID)
                .name(class_2561.method_43471("binding.cobbleverse_party_wide_action.toggle"))
                .description(class_2561.method_43471("binding.cobbleverse_party_wide_action.toggle.description"))
                .category(class_2561.method_43471("category.cobbleverse_party_wide_action"))
                .allowedContexts(BindContext.IN_GAME)
                .radialCandidate(ACTION_ID)
                .addKeyCorrelation(key)
                .keyEmulation(key));
    }

    @Override
    public void onControllersDiscovered(dev.isxander.controlify.api.ControlifyApi api) {
    }

    @Override
    public void onControlifyPreInit(dev.isxander.controlify.api.entrypoint.PreInitContext context) {
    }

    private static void toggleParty(class_310 client) {
        if (client.field_1724 == null || client.field_1755 != null) return;

        var party = com.cobblemon.mod.common.client.CobblemonClient.INSTANCE.getStorage().getParty();
        boolean anyActive = false;
        for (int slot = 0; slot < 6; slot++) {
            Pokemon pokemon = party.get(slot);
            if (pokemon != null && pokemon.getState() instanceof ActivePokemonState) {
                anyActive = true;
                break;
            }
        }

        for (int slot = 0; slot < 6; slot++) {
            Pokemon pokemon = party.get(slot);
            if (pokemon == null || pokemon.isFainted()) continue;
            boolean active = pokemon.getState() instanceof ActivePokemonState;
            if (!anyActive || active) {
                CobblemonNetwork.sendToServer(new SendOutPokemonPacket(slot));
            }
        }
    }
}
