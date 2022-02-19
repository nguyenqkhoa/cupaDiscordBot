package elementale;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Instant;
import java.util.*;

public final class cupaBot {

    private static final Map<String, commands.Command> Commands = new HashMap<>();

    static {
        Commands.put("ping", event -> event.getMessage()
                .getChannel().block()
                .createMessage("Pong!").block());
    }

    public static void main(final String[] args) throws FileNotFoundException {

        final DiscordClient client = DiscordClient.create(args[0]);
        final GatewayDiscordClient gateway = client.login().block();
        File file = new File(System.getProperty("user.dir") + "\\cupaPictures.txt");
        //File file = new File("C:\\Users\\nguye\\IdeaProjects\\cupaDiscordBot\\src\\main\\java\\elementale\\cupaPictures.txt");
        Scanner input = new Scanner(file);
        List<String> list = new ArrayList<String>();

        while (input.hasNextLine()) {
            list.add(input.nextLine());
        }

        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            final Message message = event.getMessage();
            if("!Cupa".equals(message.getContent())) {
                Random random = new Random();
                int min = 1;
                int max = list.size();
                System.out.println(max);
                int num = random.nextInt(max - min) + min;
                String link = list.get(num);
                System.out.println(num);

                EmbedCreateSpec embed = EmbedCreateSpec.builder()
                        .color(Color.BLUE)
                        .title("Cupa")
                        .description("Cupa Creeper #" + num + "!")
                        .image(link)
                        .timestamp(Instant.now())
                        .build();

                EmbedCreateSpec finalEmbed = embed;
                final MessageChannel channel = message.getChannel().block();
                channel.createMessage(finalEmbed).block();
            }

            /*
            if ("!Cupa".equals(message.getContent())) {
                final MessageChannel channel = message.getChannel().block();
                channel.createMessage("https://imgur.com/QJNLicf").block();
            }

             */
        });



        gateway.getEventDispatcher().on(MessageCreateEvent.class)
                // subscribe is like block, in that it will *request* for action
                // to be done, but instead of blocking the thread, waiting for it
                // to finish, it will just execute the results asynchronously.
                .subscribe(event -> {
                    // 3.1 Message.getContent() is a String
                    final String content = event.getMessage().getContent();

                    for (final Map.Entry<String, commands.Command> entry : Commands.entrySet()) {
                        // We will be using ! as our "prefix" to any command in the system.
                        if (content.startsWith('!' + entry.getKey())) {
                            entry.getValue().execute(event);
                            break;
                        }
                    }
                });

        gateway.onDisconnect().block();
    }
}