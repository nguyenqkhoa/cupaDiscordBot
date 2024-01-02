import discord4j.core.event.domain.message.MessageCreateEvent;

import java.io.IOException;

interface Command {
    void execute(MessageCreateEvent event) throws IOException;
}
