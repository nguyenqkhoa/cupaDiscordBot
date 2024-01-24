import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TwitchIntegration {
    static Mono<Void> twitchLive(MessageCreateEvent event) {
        Message message = event.getMessage();
        String content = message.getContent();

        if (content.startsWith("!is_live")) {

            for (String channelName : CupaBot.twitchChannels) {
                checkTwitchOnlineStatus(message, channelName);
            }
        }
        return Mono.empty();
    }

    private static void checkTwitchOnlineStatus(Message message, String twitchChannelName) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.twitch.tv/helix/streams?user_login=" + twitchChannelName))
                .header("Client-ID", CupaBot.twitchClientID)
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> handleTwitchResponseInDiscord(message, twitchChannelName, responseBody))
                .exceptionally(e -> {
                    message.getChannel().block().createMessage("An error occurred while checking Twitch status.").block();
                    return null;
                });
    }

    private static void handleTwitchResponseInDiscord(Message message, String twitchChannelName, String responseBody) {
        try {
            JsonObject json = JsonParser.parseReader(new StringReader(responseBody)).getAsJsonObject();
            if (json.has("data") && json.get("data").isJsonArray()) {
                JsonArray dataArray = json.getAsJsonArray("data");
                System.out.println("Debug: " + json);
                if (dataArray.size() > 0) {
                    message.getChannel().block().createMessage(twitchChannelName + " is currently live.").block();
                } else {
                    message.getChannel().block().createMessage(twitchChannelName + " is not live at the moment.").block();
                }
            } else {
                message.getChannel().block().createMessage("Error: Unexpected Twitch API response.").block();
                System.out.println("Debug: " + json);
            }
            if (responseBody.contains("\"type\":\"live\"")) {
                    message.getChannel().block().createMessage("https://www.twitch.tv/" + twitchChannelName + " is currently live.").block();
                } else {
                message.getChannel().block().createMessage("https://www.twitch.tv/" + twitchChannelName + " is not live at the moment.").block();
            }
        } catch (Exception e) {
            message.getChannel().block().createMessage("Haha poopie").block();
        }
    }
}