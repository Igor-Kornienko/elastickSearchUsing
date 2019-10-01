import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.queryparser.xml.builders.SpanQueryBuilderFactory;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.SpanOrQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class main {
    private static ObjectMapper mapper = new ObjectMapper();
    private static Client client;

    public static void main(String... args) throws UnknownHostException, JsonProcessingException {
        client = new PreBuiltTransportClient(
                Settings.builder().put("client.transport.sniff", true)
                        .put("cluster.name","elasticsearch").build())
                .addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"), 9300));

        //deleteTestData();
        //addTestData();
        searchAllTestData();
        //searchTestDataByBirth();
        searchTestDataByWords("smb10676", "smb8");
    }

    private static void addTestData() throws JsonProcessingException {
        List<user> userBuff = new ArrayList<user>();

        userBuff.add(new user("smb0 dsfasf", "smb0@gmail.com", "2312312313", new Date(97, 11, 10)));
        userBuff.add(new user("smb1 sdfsf", "smb1@gmail.com", "2376812313", new Date(98, 0, 10)));
        userBuff.add(new user("smb2 sdfas", "smb2@gmail.com", "2356767313", new Date(98, 1, 10)));
        userBuff.add(new user("smb3 sdfasdfs", "smb3@gmail.com", "2312456343", new Date(98, 2, 10)));
        userBuff.add(new user("smb4 asdfg", "smb4@gmail.com", "2346456363", new Date(98, 3, 10)));
        userBuff.add(new user("smb5 sdfsf", "smb5@gmail.com", "2312657856", new Date(98, 4, 10)));
        userBuff.add(new user("smb6 sdfsfssa", "smb6@gmail.com", "5678567313", new Date(98, 5, 10)));
        userBuff.add(new user("smb7 sdfsdf", "smb7@gmail.com", "2365788313", new Date(98, 6, 10)));
        userBuff.add(new user("smb8 sdfafasf", "smb8@gmail.com", "2367852313", new Date(98, 7,10)));
        userBuff.add(new user("smb9 sadfaf", "smb9@gmail.com", "2312397899", new Date(98, 8, 10)));
        userBuff.add(new user("smb10 sadfaf", "smb8@gmail.com", "2312397899", new Date(98, 8, 10)));

        for (user a : userBuff) {
            String jsonBuff = mapper.writeValueAsString(a);

            IndexResponse response = client.prepareIndex("users", "userInfo")
                    .setSource(jsonBuff, XContentType.JSON).get();
            System.out.println("Id: " + response.getId());
            System.out.println("Index: " + response.getIndex());
            System.out.println("Type: " + response.getType());
            System.out.println("Version: " + response.getVersion());
            System.out.println();
        }
    }

    private static void deleteTestData() {
        client.admin().indices().delete(new DeleteIndexRequest("users")).actionGet();
    }

    private static void searchAllTestData() {
        SearchResponse response = client.prepareSearch().execute().actionGet();
        responseSout(response);
    }

    private static void searchTestDataByBirth(){
        QueryBuilder matchQueryByBirth = QueryBuilders
                .rangeQuery("birth")
                    .from(new Date(98, 2, 10).getTime())
                    .to(new Date(98, 7,10).getTime());

        SearchResponse response = client.prepareSearch()
                .setTypes()
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setPostFilter(matchQueryByBirth)
                .execute()
                .actionGet();

        responseSout(response);
    }

    private static void searchTestDataByWords(String param1, String param2){
        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        boolQuery.must(QueryBuilders.regexpQuery("name", ".*" + param1 + ".*"));
        boolQuery.must(QueryBuilders.regexpQuery("email", ".*" + param2 + ".*"));

        SearchResponse response = client.prepareSearch()
                .setTypes()
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setPostFilter(boolQuery)
                .execute()
                .actionGet();

        responseSout(response);
    }

    private static void responseSout(SearchResponse response){
        List<SearchHit> searchHits = Arrays.asList(response.getHits().getHits());
        final List<user> results = new ArrayList<user>();
        searchHits.forEach(
                hit -> {
                    try {
                        results.add(mapper.readValue(hit.getSourceAsString(), user.class));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );

        results.forEach(
                user -> System.out.println(user.toString())
        );

        System.out.println();
    }
}
