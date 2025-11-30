package liarsdice.Actions;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import liarsdice.Server.Controller.GameController;

@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "ACTION")
public abstract class Action {

    protected String nickname;

    public abstract void execute(GameController controller);

    public void setName(String name) {
        this.nickname = name;
    }

}
