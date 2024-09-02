package com.orleansmc.realms.models.messaging;

import com.orleansmc.realms.models.config.TextModel;

public class MessageModel {
    public final String target;
    public final TextModel textModel;

    public MessageModel(String target, TextModel textModel) {
        this.target = target;
        this.textModel = textModel;
    }
}
