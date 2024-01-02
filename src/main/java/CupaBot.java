import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.util.*;

public final class CupaBot {

    private static final String danbooru = "https://danbooru.donmai.us/posts/random.json?tags=cupa_%28at2.%29";
    private static final Map<String, Command> commands = new HashMap<>();

    static {
        commands.put("ping", event -> event.getMessage()
                .getChannel().block()
                .createMessage("Pong!").block());

        commands.put("Cupa", event -> event.getMessage()
                .getChannel().block()
                .createMessage(createCupaEmbedFromPicturesFile()).block());

        commands.put("Danbooru", event -> event.getMessage()
                .getChannel().block()
                .createMessage(getDanbooruLink()).block());

    }

    public static void main(final String[] args) throws FileNotFoundException {

        final GatewayDiscordClient client = DiscordClientBuilder.create(args[0])
                .build()
                .login()
                .log("Cupa reporting!")
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
        client.onDisconnect().block();

    }

    private static EmbedCreateSpec createCupaEmbedFromPicturesFile() {
        File file = new File(System.getProperty("user.dir") + "\\cupaPictures.txt");
        List<String> cupaPictureList = new ArrayList<>();
        Scanner input = null;
        try {
            input = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (input.hasNextLine()) {
            cupaPictureList.add(input.nextLine());
        }

        Random random = new Random();

        int min = 1;
        int max = cupaPictureList.size();
        System.out.println(max);
        int num = random.nextInt(max - min) + min;
        String link = cupaPictureList.get(num);
        System.out.println(num);

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