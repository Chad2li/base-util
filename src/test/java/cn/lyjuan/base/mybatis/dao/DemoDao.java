package cn.lyjuan.base.mybatis.dao;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Table;

/**
 * @author chad
 * @date 2022/1/11 18:02
 * @since
 */
@Data
@ToString
@Table(name = "demo")
public class DemoDao {
    private int id;
    private String name;
    private int age;
}
