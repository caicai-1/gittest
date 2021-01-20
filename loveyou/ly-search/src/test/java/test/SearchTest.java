package test;

import com.leyou.LySearchApplication;
import com.leyou.client.item.ItemClient;
import com.leyou.item.entity.SpecParam;
import com.leyou.search.repository.SearchRepository;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LySearchApplication.class)
public class SearchTest {

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private SearchService searchService;

    @Autowired
    private SearchRepository searchRepository;

    /**
     * 将数据源的数据写入索引库
     */
    @Test
    public void indexWrite(){
        searchService.importData();
    }

    @Test
    public void findSpecParams(){
        List<SpecParam> specParams = itemClient.findParams(null, 76L, true);
        System.out.println(specParams);
    }
}