import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeoutException;

public final class CupaBot {

    private static final String danbooru = "https://danbooru.donmai.us/posts/random.json?tags=cupa_%28at2.%29";
    private static final Map<String, Command> commands = new HashMap<>();
    private static final ReactionEmoji leftArrow = ReactionEmoji.of(282734396686204928L, "leftArrow", false);
    private static final ReactionEmoji rightArrow = ReactionEmoji.of(282734401631158273L, "rightArrow", false);
    private static final Button buttonTest = Button.primary("MoreCupa", "Cupa?");
    private static final Button leftButton = Button.primary("LeftArrow", leftArrow);
    private static final Button rightButton = Button.primary("RightArrow", rightArrow);
    private static List<String> cupaPictureList = new ArrayList<>();
    private static int cupaIndex = 1;

    static {
        commands.put("ping", event -> event.getMessage()
                .getChannel().block()
                .createMessage("Pong!").block());

        commands.put("Cupa", event -> event.getMessage()
                .getChannel().block()
                .createMessage(selectRandomCupaToEmbed()).block()
                .getChannel().block()
                .createMessage(MessageCreateSpec.builder()
                        .addComponent(ActionRow.of(leftButton, buttonTest, rightButton))
                        .build())
                .subscribe());

        commands.put("Left", event -> event.getMessage()
                .getChannel().block()
                .createMessage(selectCupaToEmbed(cupaIndex)).block()
                .getChannel().block()
                .createMessage(MessageCreateSpec.builder()
                        .addComponent(ActionRow.of(leftButton, buttonTest, rightButton))
                        .build())
                .subscribe());

        commands.put("Right", event -> event.getMessage()
                .getChannel().block()
                .createMessage(selectCupaToEmbed(cupaIndex)).block()
                .getChannel().block()
                .createMessage(MessageCreateSpec.builder()
                        .addComponent(ActionRow.of(leftButton, buttonTest, rightButton))
                        .build())
                .subscribe());

        commands.put("Danbooru", event -> event.getMessage()
                .getChannel().block()
                .createMessage(getDanbooruLink()).block());



    }

    public static void main(final String[] args) throws FileNotFoundException {

        getCupaPicturesFromFile();

        final GatewayDiscordClient client = DiscordClientBuilder.create(args[0])
                .build()
                .login()
                .block();

        client.getEventDispatcher().on(MessageCreateEvent.class)
                .subscribe(event -> {
                            final String message = event.getMessage().getContent();
                            for (final Map.Entry<String, Command> entry : commands.entrySet()) {
                                if (message.startsWith('!' + entry.getKey())) {
                                    try {
                                        entry.getValue().execute(event);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                }
                            }
                        });

        Snowflake channelId = Snowflake.of(944693683905769536L);

        client.getChannelById(channelId)
                .ofType(GuildMessageChannel.class)
                .flatMap(channel -> {

                    Mono<Message> createMessageMono = channel.createMessage(MessageCreateSpec.builder()
                            .addComponent(ActionRow.of(buttonTest))
                            .build());

                    Mono<Void> tempListener = client.on(ButtonInteractionEvent.class, event -> {
                                if(event.getCustomId().equals("MoreCupa")){
                                    System.out.println("More!");
                                    return event.reply("!Cupa").withEphemeral(true);
                                } else if(event.getCustomId().equals("LeftArrow")){
                                    System.out.println("Left!");
                                    cupaIndex--;
                                    return event.reply("!Left").withEphemeral(true);
                                }
                                else if(event.getCustomId().equals("RightArrow")){
                                    System.out.println("Right!");
                                    cupaIndex++;
                                    return event.reply("!Right").withEphemeral(true);
                                }
                                else {
                                    return Mono.empty();
                                }
                            }).timeout(Duration.ofMinutes(30))
                            .onErrorResume(TimeoutException.class, ignore -> Mono.empty())
                            .then();

                    return createMessageMono.then(tempListener);
                }).subscribe();



        client.onDisconnect().block();
    }

    private static void getCupaPicturesFromFile() {
        File file = new File(System.getProperty("user.dir") + "\\cupaPictures.txt");
        Scanner input = null;
        try {
            input = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (input.hasNextLine()) {
            cupaPictureList.add(input.nextLine());
        }

        System.out.println("Cupa pictures: " + cupaPictureList.size());
    }

    private static EmbedCreateSpec selectCupaToEmbed(int cupaIndex) {

        int max = cupaPictureList.size();
        System.out.println("Cupa index: " + cupaIndex);
        if(cupaIndex <= 0){
            cupaIndex = max;
            System.out.println("Sweeping back to the top: " + cupaIndex);
        }
        if(cupaIndex > max){
            cupaIndex = 1;
            System.out.println("Sweeping back to the start: " + cupaIndex);
        }

        String link = cupaPictureList.get(cupaIndex);

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title("Cupa")
                .description("Cupa Creeper #" + cupaIndex + "!")
                .image(link)
                .timestamp(Instant.now())
                .build();

        return embed;
    }

    private static EmbedCreateSpec selectRandomCupaToEmbed() {

        Random random = new Random();

        int min = 1;
        int max = cupaPictureList.size();
        System.out.println(max);
        int num = random.nextInt(max - min) + min;
        String link = cupaPictureList.get(num);
        cupaIndex = num;
        System.out.println(cupaIndex);

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title("Cupa")
                .description("Cupa Creeper #" + num + "!")
                .image(link)
                .timestamp(Instant.now())
                .build();

        return embed;
    }

    private static String getDanbooruLink() throws IOException {
        URL url = new URL(danbooru);
        URLConnection request = url.openConnection();
        request.connect();

        JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent())).getAsJsonObject();

        String file_url = jsonObject.get("file_url").getAsString();
        return file_url;
    }

}