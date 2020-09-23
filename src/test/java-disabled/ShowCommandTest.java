import com.faendir.zachtronics.bot.Application;
import com.faendir.zachtronics.bot.discord.commands.ShowCommand;
import com.faendir.zachtronics.bot.model.sc.SpaceChem;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.ReceivedMessage;
import net.dv8tion.jda.internal.entities.TextChannelImpl;
import net.dv8tion.jda.internal.entities.UserById;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.Objects;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableConfigurationProperties
@SpringBootTest(classes = Application.class, properties = "spring.main.lazy-initialization=true")
public class ShowCommandTest {

    @Autowired
    private ShowCommand showCommand;
    @Autowired
    private SpaceChem spaceChem;

    @Test
    public void testHandleMessage() {
        String command = "!show c Two By Two";

        User author = new UserById(-1);
        GuildImpl guild = new GuildImpl(null, -1);
        TextChannel channel = new TextChannelImpl(-1, guild);
        Message message = new ReceivedMessage(-1, null, null, false, false, new TLongHashSet(), new TLongHashSet(),
                                              false, false, "", "", author, null, null, null, Collections.emptyList(),
                                              Collections.emptyList(), Collections.emptyList(), 0);

        String result = showCommand.handleMessage(spaceChem, author, channel, message,
                                                  Objects.requireNonNull(showCommand.getRegex().find(command, 0)));
        System.out.println(result);
        assertNotNull(result);
    }
}