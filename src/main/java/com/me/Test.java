package com.me;

import javax.sql.DataSource;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.logging.Logger;

//枚举类也完全可以像其他类那样定义自己的字段、方法
enum World {
    // 唯一枚举:
    INSTANCE;

    private String name = "world";

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

/**
 * 装饰器:Decorator
 */
interface TextNodes {
    // 获取text:
    String getText();

    // 设置text:
    void setText(String text);
}

/**
 * 组合模式（Composite）
 */
interface Node {
    // 添加一个节点为子节点:
    Node add(Node node);

    // 获取子节点:
    List<Node> children();

    // 输出为XML:
    String toXml();
}

interface Engine {
    void start();
}

/**
 * 抽象工厂模式（Abstract Factory）
 */
//抽象工厂：生产HTML或Word
interface AbstractFactory {
    //如果把创建工厂的代码放到AbstractFactory中，就可以连实际工厂也屏蔽了
    static AbstractFactory createFactory(String name) {
        if (name.equalsIgnoreCase("fast")) {
            return new FastFactory();
        } else if (name.equalsIgnoreCase("good")) {
            return new GoodFactory();
        } else {
            throw new IllegalArgumentException("Invalid factory name");
        }
    }

    // 创建Html文档:
    HtmlDocument createHtml(String md);

    // 创建Word文档:
    WordDocument createWord(String md);
}

//抽象产品
// Html文档接口:
interface HtmlDocument {
    String toHtml();

    void save(Path path) throws IOException;
}


// Word文档接口:
interface WordDocument {
    void save(Path path) throws IOException;
}

interface NumberFactory {
    NumberFactory impl = new NumberFactoryImpl();

    // 获取工厂实例:
    static NumberFactory getFactory() {
        return impl;
    }

    // 创建方法:
    Number parse(String s);
}

public class Test {

    public static void main(String[] args) throws Exception {

        /**
         * 设计模式，即Design Patterns，是指在软件设计中，被反复使用的一种代码设计经验。
         * 使用设计模式的目的是为了可重用代码，提高代码的可扩展性和可维护性。
         *
         * 使用设计模式？
         * 根本原因还是软件开发要实现可维护、可扩展，就必须尽量复用代码，并且降低代码的耦合度。
         *
         * 设计模式主要是基于OOP编程提炼的，它基于以下几个原则：
         *  开闭原则:
         *      软件应该对扩展开放，而对修改关闭。
         *      这里的意思是在增加新功能的时候，能不改代码就尽量不要改，如果只增加代码就完成了新功能，那是最好的。
         *
         *  里氏替换原则:
         *      如果我们调用一个父类的方法可以成功，那么替换成子类调用也应该完全可以运行。
         *
         * GoF把23个常用模式分为创建型模式、结构型模式和行为型模式三类
         */

        /**
         * 创建型模式关注点是 如何 创建 对象，其核心思想是要把对象的创建和使用相分离，这样使得两者能相对独立地变换
         *
         * 创建型模式包括：
         *     工厂方法：Factory Method
         *     抽象工厂：Abstract Factory
         *     建造者：Builder
         *     原型：Prototype
         *     单例：Singleton
         */

        /**工厂方法(Factory Method)
         *
         * 定义一个用于创建对象的接口，让子类决定实例化哪一个类。Factory Method使一个类的实例化延迟到其子类
         * 实际上大多数情况下我们并不需要抽象工厂，而是通过静态方法直接返回产品,
         * 这种简化的使用静态方法创建产品的方式称为静态工厂方法（Static Factory Method）
         * 静态工厂方法广泛地应用在Java标准库中
         * 工厂方法可以隐藏创建产品的细节，且不一定每次都会真正创建产品，完全可以返回缓存的产品，从而提升速度并减少内存消耗。
         * 总是引用接口而非实现类，能允许变换子类而不影响调用方，即尽可能面向抽象编程。
         */
        NumberFactory factory = NumberFactory.getFactory();
        Number result = factory.parse("123.456");
        System.out.println(result);

        Number n = StaticNumberFactory.parse("456.789");
        System.out.println(n);

        LocalDate ld = LocalDateFactory.fromInt(20200202);
        System.out.println(ld);

        /**抽象工厂（Abstract Factory）
         *
         * 提供一个创建一系列相关或相互依赖对象的接口，而无需指定它们具体的类。
         */
        // 创建AbstractFactory，实际类型是FastFactory
        //客户端要使用Good的服务，只需要把原来的new FastFactory()切换为new GoodFactory()即可
        AbstractFactory fastFactory = new FastFactory();
        // 生成Html文档:
        HtmlDocument html = fastFactory.createHtml("#Hello\nHello, world!");
        html.save(Paths.get(".", "fast.html"));
        // 生成Word文档:
        WordDocument word = fastFactory.createWord("#Hello\nHello, world!");
        word.save(Paths.get(".", "fast.doc"));

        //如果把创建工厂的代码放到AbstractFactory中，就可以连实际工厂也屏蔽了
        AbstractFactory goodFactory = AbstractFactory.createFactory("good");


        /**建造者/生成器（Builder）
         *
         * 使用多个“小型”工厂来最终创建出一个完整对象
         * 将一个复杂对象的构建与它的表示分离，使得同样的构建过程可以创建不同的表示。
         * 很多时候，我们可以简化Builder模式，以链式调用的方式来创建对象。
         */
        String url = URLBuilder.builder() // 创建Builder
                .setDomain("www.liaoxuefeng.com") // 设置domain
                .setScheme("https") // 设置scheme
                .setPath("/") // 设置路径
                .setQuery(Map.of("a", "123", "q", "K&R")) // 设置query
                .build(); // 完成build
        System.out.println(url);
        String url2 = URLBuilder.builder() // 创建Builder
                .setDomain("www.baidu.com") // 设置domain
                .setScheme("https") // 设置scheme
                //.setPath("/") // 设置路径
                .setQuery(Map.of("aa", "123", "qq", "K&R")) // 设置query
                .build(); // 完成build
        System.out.println(url2);


        /**原型（Prototype）
         *
         * 用原型实例指定创建对象的种类，并且通过拷贝这些原型创建新的对象。
         *
         * 应用不是很广泛，因为很多实例会持有类似文件、Socket这样的资源，而这些资源是无法复制给另一个对象共享的，
         * 只有存储简单类型的“值”对象可以复制
         */
        // 原型:
        String[] original = {"Apple", "Pear", "Banana"};
        // 新对象:
        String[] copy = Arrays.copyOf(original, original.length);

        //Java的Object提供了一个clone()方法，它的意图就是复制一个新的对象出来，
        // 我们需要实现一个Cloneable接口来标识一个对象是“可复制”的
        //因为clone()的方法签名是定义在Object中，返回类型也是Object，所以要强制转型，比较麻烦
        Student std1 = new Student();
        std1.setId(123);
        std1.setName("Bob");
        std1.setScore(88);
        // 复制新对象,强制转型
        Student std2 = (Student) std1.clone();
        //实际上，使用原型模式更好的方式是定义一个copy()方法，返回明确的类型
        Student std3 = (Student) std1.clone();
        System.out.println(std1);
        System.out.println(std2);
        System.out.println(std3);
        System.out.println(std1 == std2); // false
        System.out.println(std1 == std3); // false


        /**单例模式（Singleton）
         *
         * 保证一个类仅有一个实例，并提供一个访问它的全局访问点。
         * 目的是为了保证在一个进程中，某个类有且仅有一个实例。
         *
         * 单例的构造方法必须是private，这样就防止了调用方自己创建实例，
         * 但是在类的内部，是可以用一个静态字段来引用唯一创建的实例的
         */
        System.out.println(Singleton.getInstance());
        System.out.println(Singleton.INSTANCE);

        //延迟加载，即在调用方第一次调用getInstance()时才初始化全局唯一实例
        // 这种写法在多线程中是错误的，在竞争条件下会创建出多个实例。
        // 必须对整个方法进行加锁,但加锁会严重影响并发性能
        //如果没有特殊的需求，使用Singleton模式的时候，最好不要延迟加载，这样会使代码更简单。

        //另一种实现Singleton的方式是利用Java的enum，因为Java保证枚举类的每个枚举都是单例，
        // 所以我们只需要编写一个只有一个枚举的类即可
        //使用枚举实现Singleton还避免了第一种方式实现Singleton的一个潜在问题：
        // 即序列化和反序列化会绕过普通类的private构造方法从而创建出多个实例，而枚举类就没有这个问题。
        String name = World.INSTANCE.getName();
        System.out.println(name);

        //什么时候应该用Singleton呢？实际上，很多程序，尤其是Web程序，大部分服务类都应该被视作Singleton，
        // 如果全部按Singleton的写法写，会非常麻烦，所以，通常是通过约定让框架（例如Spring）来实例化这些类，
        // 保证只有一个实例，调用方自觉通过框架获取实例而不是new操作符
        // @Component // 表示一个单例组件
        // public class MyService {
        //     ...
        // }


        /**
         * 结构型模式主要涉及 如何 组合 各种对象以便获得更好、更灵活的结构。
         * 虽然面向对象的继承机制提供了最基本的子类扩展父类的功能，但结构型模式不仅仅简单地使用继承，
         * 而更多地通过组合与运行期的动态组合来实现更灵活的功能。
         *
         * 结构型模式有：
         *     适配器:Adapter，也称Wrapper
         *     桥接
         *     组合:Composite
         *     装饰器:Decorator
         *     外观:Facade
         *     享元:Flyweight
         *     代理:Proxy
         */

        /**适配器:Adapter，也称Wrapper
         *
         * 将一个类的接口转换成客户希望的另外一个接口，使得原本由于接口不兼容而不能一起工作的那些类可以一起工作。
         *
         * 编写一个Adapter的步骤如下：
         *     实现目标接口；
         *     内部持有一个待转换接口的引用；
         *     在目标接口的实现方法内部，调用待转换接口的方法。
         */
        Callable<Long> callable = new Task(123450000L);
        Thread thread = new Thread(new RunnableAdapter(callable));
        thread.start();

        //InputStreamReader就是Java标准库提供的Adapter，它负责把一个InputStream适配为Reader。
        // 类似的还有OutputStreamWriter。
        //InputStream input = Files.newInputStream(Paths.get("/path/to/file"));
        //Reader reader = new InputStreamReader(input, "UTF-8");
        //readText(reader);

        /**桥接模式
         *
         * 将抽象部分与它的实现部分分离，使它们都可以独立地变化。
         * 桥接模式就是为了避免直接继承带来的子类爆炸。
         *
         * 桥接模式实现比较复杂，实际应用也非常少，但它提供的设计思想值得借鉴，即不要过度使用继承，
         * 而是优先拆分某些部件，使用组合的方式来扩展功能。
         */
        RefinedCar car = new BossCar(new HybridEngine());
        car.drive();

        /**组合模式（Composite）
         *
         * 将对象组合成树形结构以表示“部分-整体”的层次结构，使得用户对单个对象和组合对象的使用具有一致性。
         *
         * 使用Composite模式时，需要先统一单个节点以及“容器”节点的接口
         */
        Node root = new ElementNode("school");
        root.add(new ElementNode("classA")
                .add(new TextNode("Tom"))
                .add(new TextNode("Alice")));
        root.add(new ElementNode("classB")
                .add(new TextNode("Bob"))
                .add(new TextNode("Grace"))
                .add(new CommentNode("comment...")));
        System.out.println(root.toXml());

        /**装饰器:Decorator
         *
         * 动态地给一个对象添加一些额外的职责。就增加功能来说，相比生成子类更为灵活。
         * 是一种在运行期动态给某个对象的实例增加功能的方法。
         * 它实际上把核心功能和附加功能给分开了
         */
        //InputStream input = new GZIPInputStream( // 第二层装饰
        //        new BufferedInputStream( // 第一层装饰
        //                new FileInputStream("test.gz") // 核心功能
        //        ));

        TextNodes n1 = new SpanNode();
        TextNodes n2 = new BoldDecorator(new SpanNode());
        n1.setText("Hello");
        n2.setText("Decorated");
        System.out.println(n1.getText());
        // 输出<span>Hello</span>
        System.out.println(n2.getText());
        // 输出<b><span>Decorated</span></b>

        /**外观:Facade
         *
         * 为子系统中的一组接口提供一个一致的界面。Facade模式定义了一个高层接口，这个接口使得这一子系统更加容易使用。
         * Facade模式是为了给客户端提供一个统一入口，并对外屏蔽内部子系统的调用细节。
         */
        Facade facade = new Facade();
        Company c = facade.openCompany("Facade Software Ltd.");
        System.out.println(c);

        /**享元:Flyweight
         *
         * 运用共享技术有效地支持大量细粒度的对象。
         * 核心思想很简单：如果一个对象实例一经创建就不可变，那么反复创建相同的实例就没有必要，
         * 直接向调用方返回一个共享的实例就行，这样即节省内存，又可以减少创建对象的过程，提高运行速度。
         *
         * 总是使用工厂方法而不是new操作符创建实例，可获得享元模式的好处。
         * 享元模式的设计思想是尽量复用已创建的对象，常用于工厂方法内部的优化。
         * 实际应用中，享元模式主要应用于缓存，即客户端如果重复请求某些对象，不必每次查询数据库或者读取文件，而是直接返回内存中缓存的数据。
         */
        //以Integer为例，如果我们通过Integer.valueOf()这个静态工厂方法创建Integer实例，
        // 当传入的int范围在-128~+127之间时，会直接返回缓存的Integer实例
        Integer in1 = Integer.valueOf(100);
        Integer in2 = Integer.valueOf(100);
        System.out.println(in1 == in2); // true

        //对于Byte来说，因为它一共只有256个状态，所以，通过Byte.valueOf()创建的Byte实例，全部都是缓存对象。

        Students students1 = Students.create(1, "aaa");
        Students students2 = Students.create(2, "bbb");
        Students students3 = Students.create(1, "aaa");
        System.out.println(students1 == students2);
        System.out.println(students1 == students3);

        /**代理:Proxy
         *
         * 为其他对象提供一种代理以控制对这个对象的访问。
         *
         * Proxy广泛应用在:
         *  权限检查
         *  远程代理即Remote Proxy，
         *      本地的调用者持有的接口实际上是一个代理，这个代理负责把对接口的方法访问转换成远程调用，然后返回结果。
         *      Java内置的RMI机制就是一个完整的远程代理模式。
         *  虚代理即Virtual Proxy，
         *      它让调用者先持有一个代理对象，但真正的对象尚未创建。
         *      JDBC的连接池返回的JDBC连接（Connection对象）就可以是一个虚代理
         *  保护代理即Protection Proxy，
         *      它用代理对象控制对原始对象的访问，常用于鉴权。
         *  智能引用即Smart Reference，
         *      它也是一种代理对象，如果有很多客户端对它进行访问，通过内部的计数器可以在外部调用者都不使用后自动释放它。
         *
         * Decorator模式让调用者自己创建核心类，然后组合各种功能，而Proxy模式决不能让调用者自己创建再组合，否则就失去了代理的功能。
         * Proxy模式让调用者认为获取到的是核心类接口，但实际上是代理类。
         */
        String jdbcUrl = "jdbc:mysql://localhost:3306/test";
        String jdbcUsername = "xylx";
        String jdbcPassword = "1934";
        DataSource lazyDataSource = new LazyDataSource(jdbcUrl, jdbcUsername, jdbcPassword);
        System.out.println("get lazy connection...");
        try (Connection conn1 = lazyDataSource.getConnection()) {
            // 并没有实际打开真正的Connection
        }
        System.out.println("get lazy connection...");
        try (Connection conn2 = lazyDataSource.getConnection()) {
            // try (PreparedStatement ps = conn2.prepareStatement("SELECT * FROM students")) { // 打开了真正的Connection
            //     try (ResultSet rs = ps.executeQuery()) {
            //         while (rs.next()) {
            //             System.out.println(rs.getString("name"));
            //         }
            //     }
            // }
        }

        DataSource pooledDataSource = new PooledDataSource(jdbcUrl, jdbcUsername, jdbcPassword);
        try (Connection conn = pooledDataSource.getConnection()) {
        }
        try (Connection conn = pooledDataSource.getConnection()) {
            // 获取到的是同一个Connection
        }
        try (Connection conn = pooledDataSource.getConnection()) {
            // 获取到的是同一个Connection
        }

        /**
         * 行为型模式主要涉及算法和对象间的职责分配。通过使用对象组合，行为型模式可以描述一组对象应该如何协作来完成一个整体任务。
         *
         * 行为型模式有：
         *     责任链：Chain of Responsibility
         *     命令：Command
         *     解释器：Interpreter
         *     迭代器：Iterator
         *     中介：Mediator
         *     备忘录：Memento
         *     观察者：Observer
         *     状态：State
         *     策略:Strategy
         *     模板方法:Template Method
         *     访问者:Visitor
         */

        /**责任链：Chain of Responsibility
         *
         * 使多个对象都有机会处理请求，从而避免请求的发送者和接收者之间的耦合关系。将这些对象连成一条链，并沿着这条链传递该请求，直到有一个对象处理它为止。
         * 是一种处理请求的模式，它让多个处理器都有机会处理该请求，直到其中某个处理成功为止。责任链模式把多个处理器串成链，然后让请求在链上传递
         *
         * 有一些责任链模式，每个Handler都有机会处理Request，通常这种责任链被称为拦截器（Interceptor）或者过滤器（Filter），
         * 它的目的不是找到某个Handler处理掉Request，而是每个Handler都做一些工作
         *
         * 责任链模式经常用在拦截、预处理请求等。
         */

        /**命令：Command
         *
         * 将一个请求封装为一个对象，从而使你可用不同的请求对客户进行参数化，对请求排队或记录请求日志，以及支持可撤销的操作。
         * 命令模式的设计思想是把命令的创建和执行分离，使得调用者无需关心具体的执行过程。
         * 通过封装Command对象，命令模式可以保存已执行的命令，从而支持撤销、重做等操作。
         */

        /**解释器：Interpreter
         *
         * 给定一个语言，定义它的文法的一种表示，并定义一个解释器，这个解释器使用该表示来解释语言中的句子。
         * 解释器模式通过抽象语法树实现对用户输入的解释执行。
         * 解释器模式的实现通常非常复杂，且一般只能解决一类特定问题。
         */

        /**迭代器：Iterator
         *
         * 提供一种方法顺序访问一个聚合对象中的各个元素，而又不需要暴露该对象的内部表示。
         *
         * 实现Iterator模式的关键是返回一个Iterator对象，该对象知道集合的内部结构。
         * 我们使用Java的内部类实现这个Iterator
         */

        /**中介：Mediator
         *
         * 用一个中介对象来封装一系列的对象交互。中介者使各个对象不需要显式地相互引用，从而使其耦合松散，而且可以独立地改变它们之间的交互。
         * Mediator模式经常用在有众多交互组件的UI上。为了简化UI程序，MVC模式以及MVVM模式都可以看作是Mediator模式的扩展。
         */
        // new OrderFrame("Hanburger", "Nugget", "Chip", "Coffee");

        /**备忘录：Memento
         *
         * 在不破坏封装性的前提下，捕获一个对象的内部状态，并在该对象之外保存这个状态。
         * 备忘录模式是为了保存对象的内部状态，并在将来恢复，大多数软件提供的保存、打开，以及编辑过程中的Undo、Redo都是备忘录模式的应用。
         */

        /**观察者：Observer
         *
         * 定义对象间的一种一对多的依赖关系，当一个对象的状态发生改变时，所有依赖于它的对象都得到通知并被自动更新。
         * 又称发布-订阅模式（Publish-Subscribe：Pub/Sub）。它是一种通知机制，让发送通知的一方（被观察方）和接收通知的一方（观察者）能彼此分离，互不影响。
         */
        // observer:
        Admin admin = new Admin();
        Customer customer = new Customer();
        // store:
        Store store = new Store();
        // 注册观察者:
        store.addObserver(admin);
        store.addObserver(customer);

        /**状态：State
         *
         * 允许一个对象在其内部状态改变时改变它的行为。对象看起来似乎修改了它的类
         */
        Scanner scanner = new Scanner(System.in);
        BotContext bot = new BotContext();
        // for (;;) {
        //     System.out.print("> ");
        //     String input = scanner.nextLine();
        //     String output = bot.chat(input);
        //     System.out.println(output.isEmpty() ? "(no reply)" : "< " + output);
        // }

        /**策略:Strategy
         *
         * 定义一系列的算法，把它们一个个封装起来，并且使它们可相互替换。本模式使得算法可独立于使用它的客户而变化。
         * 在一个方法中，流程是确定的，但是，某些关键步骤的算法依赖调用方传入的策略，这样，传入不同的策略，即可获得不同的结果，大大增强了系统的灵活性。
         */
        String[] array = { "apple", "Pear", "Banana", "orange" };
        Arrays.sort(array, String::compareToIgnoreCase);
        System.out.println(Arrays.toString(array));

        DiscountContext ctx = new DiscountContext();
        // 默认使用普通会员折扣:
        BigDecimal pay1 = ctx.calculatePrice(BigDecimal.valueOf(105));
        System.out.println(pay1);

        // 使用满减折扣:
        ctx.setStrategy(new OverDiscountStrategy());
        BigDecimal pay2 = ctx.calculatePrice(BigDecimal.valueOf(105));
        System.out.println(pay2);

        // 使用Prime会员折扣:
        ctx.setStrategy(new PrimeDiscountStrategy());
        BigDecimal pay3 = ctx.calculatePrice(BigDecimal.valueOf(105));
        System.out.println(pay3);

        /**模板方法:Template Method
         *
         * 定义一个操作中的算法的骨架，而将一些步骤延迟到子类中，使得子类可以不改变一个算法的结构即可重定义该算法的某些特定步骤。
         * 主要思想是，定义一个操作的一系列步骤，对于某些暂时确定不下来的步骤，就留给子类去实现好了，这样不同的子类就可以定义出不同的步骤。
         * 为了防止子类重写父类的骨架方法，可以在父类中对骨架方法使用final。对于需要子类实现的抽象方法，一般声明为protected，使得这些方法对外部客户端不可见。
         */

        /**访问者:Visitor
         *
         * 表示一个作用于某对象结构中的各元素的操作。它使你可以在不改变各元素的类的前提下定义作用于这些元素的新操作。
         * 核心思想是为了访问比较复杂的数据结构，不去改变数据结构，而是把对数据的操作抽象出来，在“访问”的过程中以回调形式在访问者中处理操作逻辑。
         * 如果要新增一组操作，那么只需要增加一个新的访问者。
         */
        //Java标准库提供的Files.walkFileTree()已经实现了一个访问者模式
        Files.walkFileTree(Paths.get("."), new MyFileVisitor());


    }
}


/**
 * 访问者:Visitor
 */
// 实现一个FileVisitor:
class MyFileVisitor extends SimpleFileVisitor<Path> {
    // 处理Directory:
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        System.out.println("pre visit dir: " + dir);
        // 返回CONTINUE表示继续访问:
        return FileVisitResult.CONTINUE;
    }

    // 处理File:
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        System.out.println("visit file: " + file);
        // 返回CONTINUE表示继续访问:
        return FileVisitResult.CONTINUE;
    }
}


/**
 * 策略:Strategy
 */
interface DiscountStrategy {
    // 计算折扣额度:
    BigDecimal getDiscount(BigDecimal total);
}
class UserDiscountStrategy implements DiscountStrategy {
    public BigDecimal getDiscount(BigDecimal total) {
        // 普通会员打九折:
        return total.multiply(new BigDecimal("0.1")).setScale(2, RoundingMode.DOWN);
    }
}
class OverDiscountStrategy implements DiscountStrategy {
    public BigDecimal getDiscount(BigDecimal total) {
        // 满100减20优惠:
        return total.compareTo(BigDecimal.valueOf(100)) >= 0 ? BigDecimal.valueOf(20) : BigDecimal.ZERO;
    }
}
class PrimeDiscountStrategy implements DiscountStrategy{
    @Override
    public BigDecimal getDiscount(BigDecimal total) {
        BigDecimal bigDecimal=new OverDiscountStrategy().getDiscount(total);
        return bigDecimal.add(total.subtract(bigDecimal).multiply(new BigDecimal(0.3)).setScale(2,RoundingMode.DOWN));
    }
}
class DiscountContext {
    // 持有某个策略:
    private DiscountStrategy strategy = new UserDiscountStrategy();

    // 允许客户端设置新策略:
    public void setStrategy(DiscountStrategy strategy) {
        this.strategy = strategy;
    }

    public BigDecimal calculatePrice(BigDecimal total) {
        return total.subtract(this.strategy.getDiscount(total)).setScale(2);
    }
}


/**
 * 状态：State
 */
interface State {

    String init();

    String reply(String input);

}
class DisconnectedState implements State {
    public String init() {
        return "Bye!";
    }

    public String reply(String input) {
        return "";
    }
}
class ConnectedState implements State {
    public String init() {
        return "Hello, I'm Bob.";
    }

    public String reply(String input) {
        if (input.endsWith("?")) {
            return "Yes. " + input.substring(0, input.length() - 1) + "!";
        }
        if (input.endsWith(".")) {
            return input.substring(0, input.length() - 1) + "!";
        }
        return input.substring(0, input.length() - 1) + "?";
    }
}
class BotContext {
    private State state = new DisconnectedState();

    public String chat(String input) {
        if ("hello".equalsIgnoreCase(input)) {
            // 收到hello切换到在线状态:
            state = new ConnectedState();
            return state.init();
        } else if ("bye".equalsIgnoreCase(input)) {
            //  收到bye切换到离线状态:
            state = new DisconnectedState();
            return state.init();
        }
        return state.reply(input);
    }
}

/**
 * 观察者：Observer
 */
class Product {
    private final String name;
    private double price;

    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return this.name;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}

//观察者
class ProductObserver {

    public void onPublished(Product p) {
    }

    public void onPriceChanged(Product p) {
    }
}

//Store不能直接引用Customer和Admin，相反，它引用一个ProductObserver接口，
// 任何人想要观察Store，只要实现该接口，并且把自己注册到Store即可
class Store {
    private final List<ProductObserver> observers = new ArrayList<>();
    private final Map<String, Product> products = new HashMap<>();

    // 注册观察者:
    public void addObserver(ProductObserver observer) {
        this.observers.add(observer);
    }

    // 取消注册:
    public void removeObserver(ProductObserver observer) {
        this.observers.remove(observer);
    }

    public void addNewProduct(String name, double price) {
        Product p = new Product(name, price);
        products.put(p.getName(), p);
        // 通知观察者:
        observers.forEach(o -> o.onPublished(p));
    }

    public void setProductPrice(String name, double price) {
        Product p = products.get(name);
        p.setPrice(price);
        // 通知观察者:
        observers.forEach(o -> o.onPriceChanged(p));
    }
}

class Admin extends ProductObserver {

}

class Customer extends ProductObserver {

}

/**
 * 中介：Mediator
 */
class OrderFrame extends JFrame {
    public OrderFrame(String... names) {
        setTitle("Order");
        setSize(460, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container c = getContentPane();
        c.setLayout(new FlowLayout(FlowLayout.LEADING, 20, 20));
        c.add(new JLabel("Use Mediator Pattern"));
        List<JCheckBox> checkboxList = addCheckBox(names);
        JButton selectAll = addButton("Select All");
        JButton selectNone = addButton("Select None");
        selectNone.setEnabled(false);
        JButton selectInverse = addButton("Inverse Select");
        new Mediator(checkboxList, selectAll, selectNone, selectInverse);
        setVisible(true);
    }

    private List<JCheckBox> addCheckBox(String... names) {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Menu:"));
        List<JCheckBox> list = new ArrayList<>();
        for (String name : names) {
            JCheckBox checkbox = new JCheckBox(name);
            list.add(checkbox);
            panel.add(checkbox);
        }
        getContentPane().add(panel);
        return list;
    }

    private JButton addButton(String label) {
        JButton button = new JButton(label);
        getContentPane().add(button);
        return button;
    }
}

//引用4个UI组件，并负责跟它们交互
class Mediator {
    // 引用UI组件:
    private final List<JCheckBox> checkBoxList;
    private final JButton selectAll;
    private final JButton selectNone;
    private final JButton selectInverse;

    public Mediator(List<JCheckBox> checkBoxList, JButton selectAll, JButton selectNone, JButton selectInverse) {
        this.checkBoxList = checkBoxList;
        this.selectAll = selectAll;
        this.selectNone = selectNone;
        this.selectInverse = selectInverse;
        this.checkBoxList.forEach(checkBox -> {
            checkBox.addChangeListener(this::onCheckBoxChanged);
        });
        this.selectAll.addActionListener(this::onSelectAllClicked);
        this.selectNone.addActionListener(this::onSelectNoneClicked);
        this.selectInverse.addActionListener(this::onSelectInverseClicked);
    }

    public void onCheckBoxChanged(ChangeEvent event) {
        boolean allChecked = true;
        boolean allUnchecked = true;
        for (var checkBox : checkBoxList) {
            if (checkBox.isSelected()) {
                allUnchecked = false;
            } else {
                allChecked = false;
            }
        }
        selectAll.setEnabled(!allChecked);
        selectNone.setEnabled(!allUnchecked);
    }

    public void onSelectAllClicked(ActionEvent event) {
        checkBoxList.forEach(checkBox -> checkBox.setSelected(true));
        selectAll.setEnabled(false);
        selectNone.setEnabled(true);
    }

    public void onSelectNoneClicked(ActionEvent event) {
        checkBoxList.forEach(checkBox -> checkBox.setSelected(false));
        selectAll.setEnabled(true);
        selectNone.setEnabled(false);
    }

    public void onSelectInverseClicked(ActionEvent event) {
        checkBoxList.forEach(checkBox -> checkBox.setSelected(!checkBox.isSelected()));
        onCheckBoxChanged(null);
    }
}

/**
 * 迭代器：Iterator
 */
class ReverseArrayCollection<T> implements Iterable<T> {
    private final T[] array;

    public ReverseArrayCollection(T... objs) {
        this.array = Arrays.copyOfRange(objs, 0, objs.length);
    }

    public Iterator<T> iterator() {
        return new ReverseIterator();
    }

    //使用内部类的好处是内部类隐含地持有一个它所在对象的this引用，可以通过ReverseArrayCollection.this引用到它所在的集合。
    class ReverseIterator implements Iterator<T> {
        // 索引位置:
        int index;

        public ReverseIterator() {
            // 创建Iterator时,索引在数组末尾:
            this.index = ReverseArrayCollection.this.array.length;
        }

        public boolean hasNext() {
            // 如果索引大于0,那么可以移动到下一个元素(倒序往前移动):
            return index > 0;
        }

        public T next() {
            // 将索引移动到下一个元素并返回(倒序往前移动):
            index--;
            return array[index];
        }
    }
}

/**
 * 代理:Proxy
 */
//对Connection接口做一个抽象的代理类,把Connection接口定义的方法全部实现一遍，因为Connection接口定义的方法太多了，
// 后面我们要编写的LazyConnectionProxy只需要继承AbstractConnectionProxy，就不必再把Connection接口方法挨个实现一遍
abstract class AbstractConnectionProxy implements Connection {

    public AbstractConnectionProxy() {
        super();
    }

    // 抽象方法获取实际的Connection:
    protected abstract Connection getRealConnection();

    // 实现Connection接口的每一个方法:
    public Statement createStatement() throws SQLException {
        return getRealConnection().createStatement();
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return getRealConnection().prepareStatement(sql);
    }

    //Connection接口定义的其他方法全部实现一遍,so much.
    @Override
    public void beginRequest() throws SQLException {

    }

    @Override
    public void endRequest() throws SQLException {

    }

    @Override
    public boolean setShardingKeyIfValid(ShardingKey shardingKey, ShardingKey superShardingKey, int timeout) throws SQLException {
        return false;
    }

    @Override
    public boolean setShardingKeyIfValid(ShardingKey shardingKey, int timeout) throws SQLException {
        return false;
    }

    @Override
    public void setShardingKey(ShardingKey shardingKey, ShardingKey superShardingKey) throws SQLException {

    }

    @Override
    public void setShardingKey(ShardingKey shardingKey) throws SQLException {

    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    public CallableStatement prepareCall(String s) throws SQLException {
        return null;
    }

    @Override
    public String nativeSQL(String s) throws SQLException {
        return null;
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return false;
    }

    @Override
    public void setAutoCommit(boolean b) throws SQLException {

    }

    @Override
    public void commit() throws SQLException {

    }

    @Override
    public void rollback() throws SQLException {

    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return null;
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return false;
    }

    @Override
    public void setReadOnly(boolean b) throws SQLException {

    }

    @Override
    public String getCatalog() throws SQLException {
        return null;
    }

    @Override
    public void setCatalog(String s) throws SQLException {

    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return 0;
    }

    @Override
    public void setTransactionIsolation(int i) throws SQLException {

    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public Statement createStatement(int i, int i1) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String s, int i, int i1) throws SQLException {
        return null;
    }

    @Override
    public CallableStatement prepareCall(String s, int i, int i1) throws SQLException {
        return null;
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return null;
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {

    }

    @Override
    public int getHoldability() throws SQLException {
        return 0;
    }

    @Override
    public void setHoldability(int i) throws SQLException {

    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return null;
    }

    @Override
    public Savepoint setSavepoint(String s) throws SQLException {
        return null;
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {

    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {

    }

    @Override
    public Statement createStatement(int i, int i1, int i2) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String s, int i, int i1, int i2) throws SQLException {
        return null;
    }

    @Override
    public CallableStatement prepareCall(String s, int i, int i1, int i2) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String s, int i) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String s, int[] ints) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String s, String[] strings) throws SQLException {
        return null;
    }

    @Override
    public Clob createClob() throws SQLException {
        return null;
    }

    @Override
    public Blob createBlob() throws SQLException {
        return null;
    }

    @Override
    public NClob createNClob() throws SQLException {
        return null;
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return null;
    }

    @Override
    public boolean isValid(int i) throws SQLException {
        return false;
    }

    @Override
    public void setClientInfo(String s, String s1) throws SQLClientInfoException {

    }

    @Override
    public String getClientInfo(String s) throws SQLException {
        return null;
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return null;
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {

    }

    @Override
    public Array createArrayOf(String s, Object[] objects) throws SQLException {
        return null;
    }

    @Override
    public Struct createStruct(String s, Object[] objects) throws SQLException {
        return null;
    }

    @Override
    public String getSchema() throws SQLException {
        return null;
    }

    @Override
    public void setSchema(String s) throws SQLException {

    }

    @Override
    public void abort(Executor executor) throws SQLException {

    }

    @Override
    public void setNetworkTimeout(Executor executor, int i) throws SQLException {

    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return 0;
    }

    @Override
    public <T> T unwrap(Class<T> aClass) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return false;
    }

}

//懒连接代理
class LazyConnectionProxy extends AbstractConnectionProxy {
    private final Supplier<Connection> supplier;
    private Connection target = null;

    public LazyConnectionProxy(Supplier<Connection> supplier) {
        this.supplier = supplier;
    }

    // 覆写close方法：只有target不为null时才需要关闭:
    public void close() throws SQLException {
        if (target != null) {
            System.out.println("Close connection: " + target);
            super.close();
        }
    }

    @Override
    protected Connection getRealConnection() {
        if (target == null) {
            target = supplier.get();
        }
        return target;
    }
}

//懒连接维护
class LazyDataSource implements DataSource {
    private final String url;
    private final String username;
    private final String password;

    public LazyDataSource(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return new LazyConnectionProxy(() -> {
            try {
                System.out.println("pre open");
                Connection conn = DriverManager.getConnection(url, username, password);
                System.out.println("Open connection: " + conn);
                return conn;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Connection getConnection() throws SQLException {
        return null;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter printWriter) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setLoginTimeout(int i) throws SQLException {

    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> aClass) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return false;
    }
}

//实现可复用Connection的连接池
class PooledConnectionProxy extends AbstractConnectionProxy {
    // 实际的Connection:
    Connection target;
    // 空闲队列:
    Queue<PooledConnectionProxy> idleQueue;

    public PooledConnectionProxy(Queue<PooledConnectionProxy> idleQueue, Connection target) {
        this.idleQueue = idleQueue;
        this.target = target;
    }

    public void close() throws SQLException {
        System.out.println("Fake close and released to idle queue for future reuse: " + target);
        // 并没有调用实际Connection的close()方法,
        // 而是把自己放入空闲队列:
        idleQueue.offer(this);
    }

    protected Connection getRealConnection() {
        return target;
    }
}

//空闲队列由PooledDataSource负责维护
class PooledDataSource implements DataSource {
    private final String url;
    private final String username;
    private final String password;

    // 维护一个空闲队列:
    private final Queue<PooledConnectionProxy> idleQueue = new ArrayBlockingQueue<>(100);

    public PooledDataSource(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Connection getConnection(String username, String password) throws SQLException {
        // 首先试图获取一个空闲连接:
        PooledConnectionProxy conn = idleQueue.poll();
        if (conn == null) {
            // 没有空闲连接时，打开一个新连接:
            conn = openNewConnection();
        } else {
            System.out.println("Return pooled connection: " + conn.target);
        }
        return conn;
    }

    private PooledConnectionProxy openNewConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(url, username, password);
        System.out.println("Open new connection: " + conn);
        return new PooledConnectionProxy(idleQueue, conn);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return null;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter printWriter) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setLoginTimeout(int i) throws SQLException {

    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> aClass) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return false;
    }

}

/**
 * 享元:Flyweight
 */
class Students {
    // 持有缓存:
    private static final Map<String, Students> cache = new HashMap<>();
    private final int id;
    private final String name;

    private Students(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // 静态工厂方法:
    public static Students create(int id, String name) {
        String key = id + "\n" + name;
        // 先查找缓存:
        Students std = cache.get(key);
        if (std == null) {
            // 未找到,创建新对象:
            System.out.println(String.format("create new Student(%s, %s)", id, name));
            std = new Students(id, name);
            // 放入缓存:
            cache.put(key, std);
        } else {
            // 缓存中存在:
            System.out.println(String.format("return cached Student(%s, %s)", std.id, std.name));
        }
        return std;
    }
}

/**
 * 外观:Facade
 */
// 工商注册:
class AdminOfIndustry {
    public Company register(String name) {
        return new Company((String.valueOf((int) (Math.random() * 10000))), name);
    }
}

// 银行开户:
class Bank {
    public String openAccount(String companyId) {
        return String.valueOf((int) (Math.random() * 100000));
    }
}

// 纳税登记:
class Taxation {
    public String applyTaxCode(String companyId) {
        return String.valueOf((int) (Math.random() * 100000));
    }
}

//公司
class Company {
    private final String id;
    private final String name;
    private String taxCode;
    private String bankAccount;

    public Company(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    @Override
    public String toString() {
        return "Company{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", taxCode='" + taxCode + '\'' +
                ", bankAccount='" + bankAccount + '\'' +
                '}';
    }
}

//注册公司的“中介”服务
class Facade {
    private final AdminOfIndustry admin = new AdminOfIndustry();
    private final Bank bank = new Bank();
    private final Taxation taxation = new Taxation();

    public Company openCompany(String name) {
        Company c = this.admin.register(name);
        String bankAccount = this.bank.openAccount(c.getId());
        c.setBankAccount(bankAccount);
        String taxCode = this.taxation.applyTaxCode(c.getId());
        c.setTaxCode(taxCode);
        return c;
    }
}

class SpanNode implements TextNodes {
    private String text;

    public String getText() {
        return "<span>" + text + "</span>";
    }

    public void setText(String text) {
        this.text = text;
    }
}

abstract class NodeDecorator implements TextNodes {
    protected final TextNodes target;

    protected NodeDecorator(TextNodes target) {
        this.target = target;
    }

    public void setText(String text) {
        this.target.setText(text);
    }
}

class BoldDecorator extends NodeDecorator {
    public BoldDecorator(TextNodes target) {
        super(target);
    }

    public String getText() {
        return "<b>" + target.getText() + "</b>";
    }
}

class ElementNode implements Node {
    private final String name;
    private final List<Node> list = new ArrayList<>();

    public ElementNode(String name) {
        this.name = name;
    }

    public Node add(Node node) {
        list.add(node);
        return this;
    }

    public List<Node> children() {
        return list;
    }

    public String toXml() {
        String start = "<" + name + ">\n";
        String end = "</" + name + ">\n";
        StringJoiner sj = new StringJoiner("", start, end);
        list.forEach(node -> {
            sj.add(node.toXml() + "\n");
        });
        return sj.toString();
    }
}

class TextNode implements Node {
    private final String text;

    public TextNode(String text) {
        this.text = text;
    }

    public Node add(Node node) {
        throw new UnsupportedOperationException();
    }

    public List<Node> children() {
        return List.of();
    }

    public String toXml() {
        return text;
    }
}

class CommentNode implements Node {
    private final String text;

    public CommentNode(String text) {
        this.text = text;
    }

    public Node add(Node node) {
        throw new UnsupportedOperationException();
    }

    public List<Node> children() {
        return List.of();
    }

    public String toXml() {
        return "<!-- " + text + " -->";
    }
}

/**
 * 桥接模式
 */
abstract class Car {
    // 引用Engine:
    protected Engine engine;

    public Car(Engine engine) {
        this.engine = engine;
    }

    public abstract void drive();
}

class HybridEngine implements Engine {
    public void start() {
        System.out.println("Start Hybrid Engine...");
    }
}

abstract class RefinedCar extends Car {
    public RefinedCar(Engine engine) {
        super(engine);
    }

    public void drive() {
        this.engine.start();
        System.out.println("Drive " + getBrand() + " car...");
    }

    public abstract String getBrand();
}

class BossCar extends RefinedCar {
    public BossCar(Engine engine) {
        super(engine);
    }

    public String getBrand() {
        return "Boss";
    }
}

/**
 * 适配器:Adapter，也称Wrapper
 */
class Task implements Callable<Long> {
    private final long num;

    public Task(long num) {
        this.num = num;
    }

    public Long call() {
        long r = 0;
        for (long n = 1; n <= this.num; n++) {
            r = r + n;
        }
        System.out.println("Result: " + r);
        return r;
    }
}

//适配器
class RunnableAdapter implements Runnable {
    // 引用待转换接口:
    private final Callable<?> callable;

    public RunnableAdapter(Callable<?> callable) {
        this.callable = callable;
    }

    // 实现指定接口:
    public void run() {
        // 将指定接口调用委托给转换接口调用:
        try {
            callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

/**
 * 单例模式（Singleton）
 */
class Singleton {
    // 静态字段引用唯一实例,可以直接把static变量暴露给外部
    public static final Singleton INSTANCE = new Singleton();

    // private构造方法保证外部无法实例化
    private Singleton() {
    }

    // 也可以通过静态方法返回实例
    public static Singleton getInstance() {
        return INSTANCE;
    }
}

/**
 * 原型（Prototype）
 */
class Student implements Cloneable {
    private int id;
    private String name;
    private int score;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    // 手动复制新对象并返回Object
    public Object clone() {
        Student std = new Student();
        std.id = this.id;
        std.name = this.name;
        std.score = this.score;
        return std;
    }

    //返回明确的类型
    public Student copy() {
        Student std = new Student();
        std.id = this.id;
        std.name = this.name;
        std.score = this.score;
        return std;
    }

}

/**
 * 建造者/生成器（Builder）
 */
class HtmlBuilder {
    private final HeadingBuilder headingBuilder = new HeadingBuilder();
    private final HrBuilder hrBuilder = new HrBuilder();
    private final ParagraphBuilder paragraphBuilder = new ParagraphBuilder();
    private final QuoteBuilder quoteBuilder = new QuoteBuilder();

    //根据特性把每一行都“委托”给一个XxxBuilder去转换，最后，把所有转换的结果组合起来，返回给客户端
    //只需要针对每一种类型编写不同的Builder
    public String toHtml(String markdown) {
        StringBuilder buffer = new StringBuilder();
        markdown.lines().forEach(line -> {
            if (line.startsWith("#")) {
                buffer.append(headingBuilder.buildHeading(line)).append('\n');
            } else if (line.startsWith(">")) {
                buffer.append(quoteBuilder.buildQuote(line)).append('\n');
            } else if (line.startsWith("---")) {
                buffer.append(hrBuilder.buildHr(line)).append('\n');
            } else {
                buffer.append(paragraphBuilder.buildParagraph(line)).append('\n');
            }
        });
        return buffer.toString();
    }
}

class HeadingBuilder {
    public String buildHeading(String line) {
        int n = 0;
        while (line.charAt(0) == '#') {
            n++;
            line = line.substring(1);
        }
        return String.format("<h%d>%s</h%d>", n, line.strip(), n);
    }
}

class HrBuilder {
    public char[] buildHr(String line) {
        return null;
    }
}

class ParagraphBuilder {
    public char[] buildParagraph(String line) {
        return null;
    }
}

class QuoteBuilder {
    public char[] buildQuote(String line) {
        return null;
    }
}

class URLBuilder {
    private String domin = "";
    private String scheme = "";
    private String path = "";
    private Map<String, String> query = new HashMap<>();

    //这里每次返回的都是新的实例
    public static URLBuilder builder() {
        return new URLBuilder();
    }

    public URLBuilder setDomain(String domin) {
        this.domin = domin;
        return this;
    }

    public URLBuilder setScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public URLBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    public URLBuilder setQuery(Map<String, String> query) {

        this.query = query;
        return this;
    }

    public String build() {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        if (this.query != null && !this.query.isEmpty()) {
            for (Map.Entry e : this.query.entrySet()) {
                sb.append(e.getKey()).append("=").append(e.getValue());
                if (i++ < this.query.size()) {
                    sb.append("&");
                }
            }
        }
        return this.scheme + "://" + this.domin + this.path + "?" + sb.toString();
    }

}

//Fast厂家
class FastFactory implements AbstractFactory {
    @Override
    public HtmlDocument createHtml(String md) {
        return new FastHtmlDocument(md);
    }

    @Override
    public WordDocument createWord(String md) {
        return new FastWordDocument(md);
    }
}

class FastHtmlDocument implements HtmlDocument {
    public FastHtmlDocument(String md) {
    }

    @Override
    public String toHtml() {
        return "";
    }

    @Override
    public void save(Path path) throws IOException {
    }
}

class FastWordDocument implements WordDocument {
    public FastWordDocument(String md) {
    }

    @Override
    public void save(Path path) throws IOException {
    }
}

//Good厂家
class GoodFactory implements AbstractFactory {
    @Override
    public HtmlDocument createHtml(String md) {
        return new GoodHtmlDocument(md);
    }

    @Override
    public WordDocument createWord(String md) {
        return new GoodWordDocument(md);
    }
}

class GoodHtmlDocument implements HtmlDocument {
    public GoodHtmlDocument(String md) {
    }

    @Override
    public String toHtml() {
        return "";
    }

    @Override
    public void save(Path path) throws IOException {
    }
}

class GoodWordDocument implements WordDocument {
    public GoodWordDocument(String md) {
    }

    @Override
    public void save(Path path) throws IOException {
    }
}

/**
 * 静态工厂方法(Static Factory Method)
 */
class LocalDateFactory {
    public static LocalDate fromInt(int yyyyMMdd) {
        //sb办法
        StringBuilder sb = new StringBuilder();
        String ymd = String.valueOf(yyyyMMdd);
        sb.append(ymd.substring(0, 4) + "-");
        sb.append(ymd.substring(4, 6) + "-");
        sb.append(ymd.substring(6));
        return LocalDate.parse(sb);
    }
}

class StaticNumberFactory {
    public static Number parse(String s) {
        return new BigDecimal(s);
    }
}

/**
 * 工厂方法(Factory Method)
 */
class NumberFactoryImpl implements NumberFactory {
    public Number parse(String s) {
        return new BigDecimal(s);
    }
}