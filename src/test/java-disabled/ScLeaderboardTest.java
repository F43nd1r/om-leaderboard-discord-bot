import com.faendir.zachtronics.bot.Application;
import com.faendir.zachtronics.bot.leaderboards.ScLeaderboard;
import com.faendir.zachtronics.bot.model.Record;
import com.faendir.zachtronics.bot.model.sc.ScCategory;
import com.faendir.zachtronics.bot.model.sc.ScPuzzle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableConfigurationProperties
@SpringBootTest(classes = Application.class, properties = "spring.main.lazy-initialization=true")
public class ScLeaderboardTest {

    @Autowired
    private ScLeaderboard scLeaderboard;

    @Test
    public void testGoodRecords() {
        Record goodRecord = scLeaderboard.get(ScPuzzle.research_example_1, ScCategory.CYCLES);
        System.out.println(goodRecord);
        goodRecord = scLeaderboard.get(ScPuzzle.published_1_1, ScCategory.SYMBOLS);
        System.out.println(goodRecord);
        goodRecord = scLeaderboard.get(ScPuzzle.published_101_3, ScCategory.RC);
        System.out.println(goodRecord);
        assertNotNull(goodRecord);
    }

    @Test
    public void testBadRecord() {
        Record badRecord = scLeaderboard.get(ScPuzzle.research_example_1, ScCategory.RC);
        assertNull(badRecord);
        badRecord = scLeaderboard.get(ScPuzzle.bonding_7, ScCategory.RCNB);
        assertNull(badRecord);
    }

}
