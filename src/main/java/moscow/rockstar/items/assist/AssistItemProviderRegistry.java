package moscow.rockstar.items.assist;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.items.assist.providers.AntiFlightProvider;
import moscow.rockstar.items.assist.providers.AssassinPotionProvider;
import moscow.rockstar.items.assist.providers.BackpackProvider;
import moscow.rockstar.items.assist.providers.BombProvider;
import moscow.rockstar.items.assist.providers.BoomTrapProvider;
import moscow.rockstar.items.assist.providers.DarkPulseProvider;
import moscow.rockstar.items.assist.providers.DisorientationProvider;
import moscow.rockstar.items.assist.providers.ExperienceScrollProvider;
import moscow.rockstar.items.assist.providers.FirecrackerPotionProvider;
import moscow.rockstar.items.assist.providers.GodAuraProvider;
import moscow.rockstar.items.assist.providers.HolyWaterProvider;
import moscow.rockstar.items.assist.providers.PaladinPotionProvider;
import moscow.rockstar.items.assist.providers.PilbProvider;
import moscow.rockstar.items.assist.providers.PlastProvider;
import moscow.rockstar.items.assist.providers.RadiationPotionProvider;
import moscow.rockstar.items.assist.providers.SleepingPotionProvider;
import moscow.rockstar.items.assist.providers.SmerchProvider;
import moscow.rockstar.items.assist.providers.SnowballProvider;
import moscow.rockstar.items.assist.providers.StunProvider;
import moscow.rockstar.items.assist.providers.TrapProvider;
import moscow.rockstar.items.assist.providers.TrapkaProvider;
import moscow.rockstar.items.assist.providers.WindChargeProvider;
import moscow.rockstar.items.assist.providers.WrathPotionProvider;

public final class AssistItemProviderRegistry {
    private AssistItemProviderRegistry() {
    }

    /** Keeps the original provider order because it affects the first matching bind. */
    public static List<AssistItemProvider> createProviders() {
        ArrayList<AssistItemProvider> providers = new ArrayList<>();
        providers.add(new TrapkaProvider());
        providers.add(new DisorientationProvider());
        providers.add(new GodAuraProvider());
        providers.add(new SmerchProvider());
        providers.add(new PlastProvider());
        providers.add(new PilbProvider());
        providers.add(new StunProvider());
        providers.add(new SnowballProvider());
        providers.add(new TrapProvider());
        providers.add(new AntiFlightProvider());
        providers.add(new DarkPulseProvider());
        providers.add(new ExperienceScrollProvider());
        providers.add(new BombProvider());
        providers.add(new BoomTrapProvider());
        providers.add(new FirecrackerPotionProvider());
        providers.add(new RadiationPotionProvider());
        providers.add(new SleepingPotionProvider());
        providers.add(new HolyWaterProvider());
        providers.add(new WrathPotionProvider());
        providers.add(new PaladinPotionProvider());
        providers.add(new AssassinPotionProvider());
        providers.add(new WindChargeProvider());
        providers.add(new BackpackProvider());
        return providers;
    }
}
