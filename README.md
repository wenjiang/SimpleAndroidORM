# SimpleAndroidORM
Android上比较简单的基于SQLite的ORM框架

###使用注意事项

使用前，先在assets文件夹下创建database.xml文件，在该文件中配置数据库：
```
<?xml version="1.0" encoding="utf-8"?>
<database>
    <!-- 数据库名称 -->
    <dbname value="zwb.db"></dbname>

    <!-- 数据库版本 -->
    <version value="1"></version>

    <!-- 数据库表 -->
    <list>
        <mapping class="com.zwb.simple.db.model.Status"></mapping>
        <mapping class="com.zwb.simple.db.model.User"></mapping>
    </list>
</database>
```
然后创建一个BaseTable的子类：

```
@Table(table = "status")
public class Status extends BaseTable {
    @Column
    @ColumnType(ColumnType = "String")
    private JSONObject text;
    @Column
    private int age;
```
@Table指定该model对应的表，@Column指定字段对应的数据库的列，如果字段类型和数据库的类型有出入，用ColumnType指定数据库类型

使用前要先调用代码：
```
DatabaseStore.getInstance().init(this);
```
接着在代码中这样使用：

保存数据:

```
   List<Status> statuses = new ArrayList<Status>();
        for (int i = 0; i < 10; i++) {
            Status status = new Status();
            try {
                JSONObject json = new JSONObject();
                json.put("name", "xbs");
                status.setText(json);
            } catch (JSONException e) {
                LogUtil.e(e.toString());
            }
            status.setAge(20);
            statuses.add(status);
        }

        try {
            DatabaseStore.getInstance().saveAll(statuses);
//         status.save();
        } catch (Exception e) {
            LogUtil.e(e.toString());
        }
```

查询数据：
```
        try {
            List<Status> statusEntities = (List<Status>) DatabaseStore.getInstance().from("status").findAll(Status.class);
            for (Status entity : statusEntities) {
                LogUtil.e(entity.getText().toString() + ":" + entity.getAge());
            }
        } catch (BaseSQLiteException e) {
            LogUtil.e(e.toString());
        }
```
这样就能找出Status这张表中的所有的数据。
删除数据:

```
DatabaseStore.from("status").deleteAll("name", "张三");
```

这样就能删除Status这张表中name字段为张三的数据。

```
status.delete();
```

表示删除该记录。

其他的用法可以看具体的源码，后面会继续补充钙文档。

