package platformer.model.perks;

import java.util.ArrayList;

public class PerksManager {

    private final ArrayList<Perk> perks;
    private final int totalPerks = 16;

    public PerksManager() {
        this.perks = new ArrayList<>();
        initPerks();
    }

    private void initPerks() {
        perks.add(new Perk(0, "Perk0", "Increase gained XP by 15%", 1, "XP Bonus"));
        perks.add(new Perk(2, "Perk1", "Reduce attack cooldown by 30%", 2, "Strong Arms"));
        perks.add(new Perk(4, "Perk2", "Unlock fire ball spell", 1, "Ancient Notes"));
        perks.add(new Perk(6, "Perk3", "Increase power capacity by 20%", 1, "Power Pills"));
        perks.add(new Perk(7, "Perk4", "Get more coins from loot", 1, "Lucky Drop"));
        perks.add(new Perk(9, "Perk5", "Increase sword attack power by 15%", 1, "Broken Bones"));
        perks.add(new Perk(11, "Perk6", "Unlock fire transformation", 2, "Dark Magic"));
        perks.add(new Perk(13, "Perk7", "Increase max health by 5%", 1, "Warrior Heart"));
        perks.add(new Perk(15, "Perk8", "Reduce dash cooldown", 2, "Furious"));
        perks.add(new Perk(16, "Perk9", "Blocking an attack restores power", 1, "Necropolis"));
        perks.add(new Perk(17, "Perk10", "Ability to deflect projectiles", 1, "Fast Eye"));
        perks.add(new Perk(19, "Perk11", "Ability to walk in lava", 2, "Amber"));
        perks.add(new Perk(20, "Perk12", "Increase power by 5%", 1, "Dragon Fruit"));
        perks.add(new Perk(22, "Perk13", "Increase critical hit chance by 5%", 2, "Elementary Magic"));
        perks.add(new Perk(24, "Perk14", "Dashing damage enemies", 3, "Unbreakable Bones"));
        perks.add(new Perk(27, "Perk15", "Increase max health by 10%", 3, "God's Blood"));
    }

    public ArrayList<Perk> getPerks() {
        return perks;
    }
}
