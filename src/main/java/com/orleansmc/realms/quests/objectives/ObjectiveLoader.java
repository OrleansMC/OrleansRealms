package com.orleansmc.realms.quests.objectives;

public class ObjectiveLoader {
    public static void load() {
        CreateRealmObjective.load();
        ConvertGemObjective.load();
        IncreaseRealmSizeObjective.load();
        InviteMembersObjective.load();
    }
}
