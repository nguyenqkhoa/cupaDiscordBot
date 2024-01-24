import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeoutException;

public final class CupaBot {

    private static final String danbooru = "https://danbooru.donmai.us/posts/random.json?tags=cupa_%28at2.%29";
    private static final Map<String, Command> commands = new HashMap<>();
    private static final ReactionEmoji leftArrow = ReactionEmoji.of(282734396686204928L, "leftArrow", false);
    private static final ReactionEmoji rightArrow = ReactionEmoji.of(282734401631158273L, "rightArrow", false);
    private static final ReactionEmoji driveLeft = ReactionEmoji.of(281283810895724554L, "driveLeft", false);
    private static final ReactionEmoji rightArrow2 = ReactionEmoji.of(282734407587069952L, "rightArrow2", false);
    private static final Button buttonCupa = Button.primary("MoreCupa", "Cupa?");
    private static final Button buttonCupaGoogleDrive = Button.primary("CupaDrive", "Cupa Drive?");
    private static final Button leftButton = Button.primary("LeftArrow", leftArrow);
    private static final Button rightButton = Button.primary("RightArrow", rightArrow);
    private static final Button driveLeftButton = Button.primary("driveLeft", driveLeft);
    private static final Button rightButton2 = Button.primary("RightArrow2", rightArrow2);
    private static final List<String> cupaPictureList = new ArrayList<>();
    private static List<com.google.api.services.drive.model.File> imageFiles;
    private static int cupaIndex = 1;
    private static int cupaDriveIndex = 1;
    private static final Snowflake botChannelId = Snowflake.of(944693683905769536L);
    public static final String twitchClientID = System.getenv("twitchClientId");
    private static final String discordToken = System.getenv("discordToken");
    public static final ArrayList<String> twitchChannels = new ArrayList<>(Arrays.asList("nguyenqkhoa", "vitelotte", "deathbymattam"));

    static {
        commands.put("ping", event -> event.getMessage()
                .getChannel().block()
                .createMessage("Pong!").block());

        commands.put("Cupa", event -> event.getMessage()
                .getChannel().block()
                .createMessage(selectRandomCupaToEmbed()).block()
                .getChannel().block()
                .createMessage(MessageCreateSpec.builder()
                        .addComponent(ActionRow.of(leftButton, buttonCupa, rightButton))
                        .build())
                .subscribe());

        commands.put("Left", event -> event.getMessage()
                .getChannel().block()
                .createMessage(selectCupaToEmbed()).block()
                .getChannel().block()
                .createMessage(MessageCreateSpec.builder()
                        .addComponent(ActionRow.of(leftButton, buttonCupa, rightButton))
                        .build())
                .subscribe());

        commands.put("Right", event -> event.getMessage()
                .getChannel().block()
                .createMessage(selectCupaToEmbed()).block()
                .getChannel().block()
                .createMessage(MessageCreateSpec.builder()
                        .addComponent(ActionRow.of(leftButton, buttonCupa, rightButton))
                        .build())
                .subscribe());

        commands.put("CupaDrive", event -> event.getMessage()
                .getChannel().block()
                .createMessage(selectCupaToEmbedFromGoogleDrive()).block()
                .getChannel().block()
                .createMessage(MessageCreateSpec.builder()
                        .addComponent(ActionRow.of(driveLeftButton, buttonCupaGoogleDrive, rightButton2))
                        .build())
                .subscribe());

        commands.put("DriveLeft", event -> event.getMessage()
                .getChannel().block()
                .createMessage(selectCupaToEmbedFromGoogleDrive()).block()
                .getChannel().block()
                .createMessage(MessageCreateSpec.builder()
                        .addComponent(ActionRow.of(driveLeftButton, buttonCupaGoogleDrive, rightButton2))
                        .build())
                .subscribe());

        commands.put("Right2", event -> event.getMessage()
                .getChannel().block()
                .createMessage(selectCupaToEmbedFromGoogleDrive()).block()
                .getChannel().block()
                .createMessage(MessageCreateSpec.builder()
                        .addComponent(ActionRow.of(driveLeftButton, buttonCupaGoogleDrive, rightButton2))
                        .build())
                .subscribe());

        commands.put("Danbooru", event -> event.getMessage()
                .getChannel().block()
                .createMessage(getDanbooruLink()).block());

    }

    public static void main(final String[] args){
        createGoogleDriveList();

        getCupaPicturesFromFile();

        final GatewayDiscordClient client = DiscordClientBuilder.create(discordToken)
                .build()
                .login()
                .block();

        client.getEventDispatcher().on(ReadyEvent.class)
                .subscribe(event -> {
                    System.out.println("Bot is ready!");
                    setDefaultGreetingChannel(event.getClient());
                });

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

        typeToGetCupa(client);

        client.on(MessageCreateEvent.class)
                .flatMap(TwitchIntegration::twitchLive)
                .subscribe();

        client.onDisconnect().block();
    }

    private static void typeToGetCupa(GatewayDiscordClient client) {
        client.getChannelById(botChannelId)
                .ofType(GuildMessageChannel.class)
                .flatMap(channel -> {

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
                                } else if(event.getCustomId().equals("CupaDrive")) {
                                    System.out.println("MORE CUPA!");
                                    return event.reply("!CupaDrive").withEphemeral(true);
                                }
                                else if(event.getCustomId().equals("driveLeft")){
                                    System.out.println("DriveLeft!");
                                    cupaDriveIndex--;
                                    return event.reply("!DriveLeft").withEphemeral(true);
                                }
                                else if(event.getCustomId().equals("RightArrow2")){
                                    System.out.println("Right2!");
                                    cupaDriveIndex++;
                                    return event.reply("!Right2").withEphemeral(true);
                                }
                                else {
                                    return Mono.empty();
                                }
                            }).timeout(Duration.ofMinutes(30))
                            .onErrorResume(TimeoutException.class, ignore -> Mono.empty())
                            .then();

                    return tempListener;
                }).subscribe();
    }

    private static void setDefaultGreetingChannel(GatewayDiscordClient client) {
        Snowflake guildID = Snowflake.of(217095609507774464L);
        client.getGuildById(guildID).flatMap(guild ->
                        guild.getChannelById(botChannelId).ofType(TextChannel.class))
                .subscribe(channel -> {
                    System.out.println("Default greeting channel set to: " + channel.getName());
                    channel.createMessage("Cupa online!").block();
                });

    }

    private static void createGoogleDriveList(){
        try {
            String credentialsPath = (System.getProperty("user.dir") + "\\cupaimagesreal.json");
            String folderId = "1JIKFHJUOCek6-X-uVelYui8yAd4IfM4m";

            GoogleDriveImageLinks googleDriveImageLinks = new GoogleDriveImageLinks(credentialsPath);
            imageFiles = googleDriveImageLinks.listImagesInFolder(folderId);

            /*
            for (com.google.api.services.drive.model.File imageFile : imageFiles) {
                System.out.println("File Name: " + imageFile.getName());
                System.out.println("File ID: " + imageFile.getId());
                System.out.println("Download Link: " + imageFile.getWebContentLink());
                System.out.println("-----");
            }
             */
            System.out.println(imageFiles.size());
        } catch (IOException | GeneralSecurityException exception){
            exception.printStackTrace();
        }
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

    private static EmbedCreateSpec selectCupaToEmbed() {

        int max = cupaPictureList.size();
        System.out.println("Cupa index: " + cupaIndex);
        if(cupaIndex < 0){
            cupaIndex = max - 1;
            System.out.println("Sweeping back to the top: " + cupaIndex);
        }
        if(cupaIndex > max - 1){
            cupaIndex = 0;
            System.out.println("Sweeping back to the start: " + cupaIndex);
        }

        String link = cupaPictureList.get(cupaIndex);

        return EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title("Cupa")
                .description("Cupa Creeper #" + cupaIndex + "!")
                .image(link)
                .timestamp(Instant.now())
                .build();

    }

    private static EmbedCreateSpec selectCupaToEmbedFromGoogleDrive(){
        int max = imageFiles.size();
        System.out.println("Cupa drive index: " + cupaDriveIndex);
        if(cupaDriveIndex < 0){
            cupaDriveIndex = max - 1;
            System.out.println("Sweeping back to the top: " + cupaDriveIndex);
        }
        if(cupaDriveIndex > max - 1){
            cupaDriveIndex = 0;
            System.out.println("Sweeping back to the start: " + cupaDriveIndex);
        }

        String link = "https://drive.google.com/uc?id=" + imageFiles.get(cupaDriveIndex).getId();
        System.out.println(link);

        return EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title("Cupa")
                .description("Cupa Creeper #" + cupaDriveIndex + "!")
                .addField("File name: ", imageFiles.get(cupaDriveIndex).getName(), false)
                .thumbnail(link)
                .image(link)
                .timestamp(Instant.now())
                .build();

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

        return EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title("Cupa")
                .description("Cupa Creeper #" + num + "!")
                .image(link)
                .timestamp(Instant.now())
                .build();
    }

    private static String getDanbooruLink() throws IOException {
        URL url = new URL(danbooru);
        URLConnection request = url.openConnection();
        request.connect();

        JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent())).getAsJsonObject();

        return jsonObject.get("file_url").getAsString();
    }

}