package coffee.cypher.catstealer

import coffee.cypher.catstealer.extensions.CatStealerExtension
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent

private val TOKEN = env("TOKEN")

@OptIn(PrivilegedIntent::class)
suspend fun main() {
    val bot = ExtensibleBot(TOKEN) {
        extensions {
            add(::CatStealerExtension)
        }

        intents {
            +Intents.nonPrivileged
            +Intent.MessageContent
        }
    }

    bot.start()
}
