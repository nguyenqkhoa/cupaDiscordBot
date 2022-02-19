package elementale;

import discord4j.core.event.domain.message.MessageCreateEvent;

import java.util.Map;

public interface commands {


    interface  Command{
        void execute(MessageCreateEvent event);
    }

}
