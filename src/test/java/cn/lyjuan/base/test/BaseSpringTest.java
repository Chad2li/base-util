package cn.lyjuan.base.test;

import cn.lyjuan.base.SpringBoot;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)

@SpringBootTest(classes = {SpringBoot.class})
@Rollback(true)
@Transactional
@AutoConfigureMockMvc
public abstract class BaseSpringTest {
}
