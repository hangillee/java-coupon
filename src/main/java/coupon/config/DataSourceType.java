package coupon.config;

import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

@Configuration
public class DataSourceType {
    public static final String WRITER = "writer";
    public static final String READER = "reader";

    @ConfigurationProperties(prefix = "coupon.datasource.writer")
    @Bean
    public DataSource writerDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @ConfigurationProperties(prefix = "coupon.datasource.reader")
    @Bean
    public DataSource readerDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @DependsOn({"writerDataSource", "readerDataSource"})
    @Bean
    public DataSource routingDataSource(
            @Qualifier("writerDataSource") DataSource writer,
            @Qualifier("readerDataSource") DataSource reader) {
        ReadOnlyDataSourceRouter dataSourceRouter = new ReadOnlyDataSourceRouter();

        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(WRITER, writer);
        dataSourceMap.put(READER, reader);

        dataSourceRouter.setTargetDataSources(dataSourceMap);
        dataSourceRouter.setDefaultTargetDataSource(writer);

        return dataSourceRouter;
    }

    @DependsOn({"routingDataSource"})
    @Primary
    @Bean
    public DataSource dataSource(DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }
}