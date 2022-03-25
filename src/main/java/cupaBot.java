import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.util.*;

public final class cupaBot {

    private static String url = "https://danbooru.donmai.us";
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
        //
        Scanner input = new Scanner(file);
        List<String> list = new ArrayList<>();

        while (input.hasNextLine()) {
            list.add(input.nextLine());
        }

        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            final Message message = event.getMessage();
            /*
            Gets an image of Cupa from a list of images
             */
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

                final MessageChannel channel = message.getChannel().block();
                channel.createMessage(embed).block();
            }
            if("!Wife".equals(message.getContent())) {
                String big_url = url + "/posts/random.json?tags=cupa_%28at2.%29";
                /*
                JSONParser parser = new JSONParser();
                JSONObject json = null;
                */
                try {
                    URL url = new URL(big_url);
                    URLConnection request = url.openConnection();
                    request.connect();

                    // Convert to a JSON object to print data
                    JsonParser jp = new JsonParser(); //from gson
                    JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
                    JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object.
                    String file_url = rootobj.get("file_url").getAsString();
                    final MessageChannel channel = message.getChannel().block();
                    channel.createMessage(file_url).block();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

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