package cn.snowt.diary.util;


/**
 * @Author: HibaraAi
 * @Date: 2021-08-15 07:20
 * @Description: 常量类
 */
public class Constant {
    /**
     * 软件内部版本
     */
    public static final Integer INTERNAL_VERSION = 6;
    /**
     * MD5加密的前缀
     */
    public static final String PASSWORD_PREFIX = "MD5*jiami-de$qianzhui#";

    /**
     * 时间格式
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 最大密码错误次数, 达到后将受到制裁
     */
    public static final Integer MAX_LOGIN_FAIL_COUNT = 5;

    /**
     * 最大制裁等级, 达到后将删库
     */
    public static final Integer MAX_PUNISHMENT_LEVEL = 15;

    /**
     * 关于的信息
     * 这种文本应该写在外部配置文件中，而不是代码编写
     */
    public static final String STRING_ABOUT = "作者: HibaraAi\n" +
            "QQ：3192233122(注明添加缘由)\n" +
            "版本: 1.3.1\n" +
            "更新日期: 2022-05-14\n" +
            "开源代码(长按复制): https://github.com/HibaraAi/Diary";

    /**
     * 帮助信息
     */
    public static final String STRING_HELP_1 = "一、功能介绍\n" +
            "本软件“消消乐”是一个本地日记记录程序,一条日记可以记录2000字符以下的文本和最多8张配图。" +
            "每条日记都可以以评论的形式追更，评论的最大长度也是2000字符。" +
            "日记还可以同时记录所处位置和当时的天气情况(当然，现在并不支持自动读取)，" +
            "可以为日记归类标签。其中只有日记文本是不能为空的。" +
            "主界面以时间倒序、信息流的形式展示日记，可以无限下滑加载更多日记。" +
            "你也可以搜索指定日记、按时间范围等方式查找特定日记。" +
            "最后，未写完的日记可以存储到草稿箱暂存，需要注意的是，只会为你暂存日记文本。\n";
    public static final String STRING_HELP_2 = "\n二、安全相关\n" +
            "“消消乐”使用本地存储，没有软件服务器，不需要网络传输，“消消乐”也建议你关闭本软件的联网权限。" +
            "“消消乐”的安全性取决于你的Android设备的安全性，正常安卓设备下，外部程序是访问不到本程序的数据的，" +
            "且日记内容也支持加密(你得手动开启)，加密/解密密钥都是随机生成。但有存就有取，数据没有绝对安全，" +
            "就算是随机生成的密钥也是得存在软件内部的，也就是说虽然密钥只有你和软件本体两个人知道，" +
            "攻击者攻击不了你，还攻击不了软件吗。在破解了root权限或USB调试(ADB调试)模式下，攻击者可以轻易获取你的密钥，" +
            "获取你的日记内容，有了密钥，加密就形同虚设。因此，你不应该破解root权限，不应该开启“开发者模式”。" +
            "这种安全问题是“消消乐”采用本地存储导致的，无法避免。\n" +
            "备份文件的安全性，备份文件的数据直接从数据库中导出，如果你的日记没有加密那就没有加密。" +
            "如果有加密，必须配合密钥文件才能读取。备份文件的后缀名虽然改了，但它本质还是txt文件，你可以" +
            "以文本的方式打开看看加密与不加密的日记长啥样。" +
            "另外，在手机系统更新的时候，建议你备份一次，免得系统更新出什么岔子将数据弄丢了。\n" +
            "导出文件的安全性，你可以理解成导出文件就是取消了加密的备份文件，\n";
    public static final String STRING_HELP_3 = "\n三、权限使用说明\n" +
            "目前，敏感权限仅使用了“存储权限”，也就是外部存储的读写权限。你当然可以拒绝授权，" +
            "但你就只能使用部分功能了。其实想想也知道，日记配图肯定要从手机相册读取，这就用到了读权限，" +
            "读取了相片，那“消消乐”也得另外存储一份吧，你当然不希望移动、删除了图片，" +
            "甚至只是修改了日记配图的文件名就导致“消消乐”中的日记配图显示异常吧，" +
            "因此“消消乐”目前的策略就是自己存一份(后续如果有更新的话或许会考虑增加只使用原图的选项)。\n" +
            "作者使用的是Honor V10，该机器系统显示使用的权限还有“查看网络连接”、“防止手机休眠”、" +
            "“开机启动”、“控制震动”、“运行前台服务”。" +
            "因此“消消乐”建议你手动去手机系统里关闭本软件的自启和网络权限，" +
            "因为这些不是作者有意申请的，可能是使用了别人的代码，别人代码中有用到这些。\n";
    public static final String STRING_HELP_4 = "\n四、登录有关\n" +
            "登录密码请牢记，登录密码只有登录后才能在设置里更改。" +
            "不要试图猜密码的方式来找回密码，猜错5次会受到一次制裁，第一次制裁时间为1分钟，" +
            "第n次制裁时间为n^3分钟，(制裁等级的n最大为10。当内部记录的n达到15时，" +
            "软件会自动清空本软件存储的所有数据。每当输入正确密码，n重置为1。)\n";
    public static final String STRING_HELP_5 = "\n五、设置项说明\n" +
            "“清除缓存”是指删除选择图片时产生的缓存，正常使用下，软件会自动清除，" +
            "但如果你强制退出，比如你在日记编写界面，选了图片后，不按照软件的正常流程使用，" +
            "而是使用系统的多任务界面直接结束本程序，那当然不会执行本程序的自动清除缓存啦。" +
            "因此这个按钮的作用就是帮你删除软件的图片缓存。\n" +
            "“清除外部存储数据”这个按钮，你有且只有在打算彻底不再使用本软件时执行，" +
            "这个操作会彻底删除由本软件生成的外部存储数据，目前包括日记的配图文件，软件的设置文件，" +
            "生成的密钥文件和生成的备份文件。谨慎使用。另外，本软件的外部存储地址为[内部存储根目录\\Hibara\\Diary]," +
            "软件卸载时不会自动删除这个目录，你需要手动删除或者执行“清除外部存储数据”这个功能。\n" +
            "“启用加密”和“修改加密密钥”，字面意思，启用加密后，新增的日记和评论都会被加密存储。" +
            "但是这个加密只有在修改加密密钥后才有效，而且加密密钥只能修改一次，只能导出密钥一次。" +
            "加密和解密使用不同的密钥(加密使用短密钥，解密使用长密钥)，而且密钥非常长，因此密钥都是导出到txt文件中存储的，" +
            "你不能修改txt的任何信息，包括里面的换行符。你应该在密钥导出后就将其移动到其他地方存储，" +
            "并妥善保管。密钥一旦丢失，你将来不能备份和恢复日记。\n" +
            "测试区的功能并未开放，开启需要测试码，如何获得测试码自行猜测。\n";
    public static final String STRING_HELP_6 = "\n六、Q&A\n" +
            "Q1：为什么还要内嵌一个扫雷游戏?\n" +
            "A：在登录界面，你可以输入“123”登录到扫雷游戏，另外“123”不属于正确密码。" +
            "扫雷可能有挺多BUG，不打算修的了。\n" +
            "Q2：为什么这么卡？\n" +
            "A：本软件只考虑功能实现，并不考虑性能优化。另外本软件没有任何进度提示，所以看起来很卡，" +
            "例如查询所有日记时，很多日记需要解密，要花大量时间，合格软件应该提供进度条展示，" +
            "而本软件没有进度提示(没错，我太菜，还懒得学)，只能干等，所以还请耐心等待。关闭加密应该会流畅很多。\n"+
            "Q3：为什么我点了“回到顶部”按钮并没有回到顶部？\n" +
            "A：别问，问就是BUG，目前来看这是偶发BUG，问题不大，不修。多点一下就可以回到顶部。\n" +
            "Q4：为什么有的图片不能保存？\n" +
            "A：日记配图是支持保存的，其他图片不能保存。" +
            "如果你特别想存某张图，可以到Hibara\\Diary\\image目录下根据文件日期找原始文件，" +
            "虽然文件后缀名为.hibara。它本质还是图片，没有经过加密的，直接复制走后再修改后缀名即可。" +
            "如果是非日记配图的图，应该在Config目录下。纪念日配图在SpecialDay目录下。软件自带的图全部不能导出。\n" +
            "Q5：我要怎么反映BUG？\n" +
            "A：请在开源代码的地址那里反映(“关于”那里复制网址打开后，" +
            "点“Issues”，之后你应该看得懂的了)。或者发送邮件到3192233122@qq.com，邮件标题注明“消消乐BUG反馈”。\n" +
            "Q6：为什么我从备份文件里恢复的日记没有图片？\n" +
            "A：因为备份文件不会携带图片，只会携带图片的存储地址，况且配图已经存在你能访问的外部存储当中，" +
            "将整个image目录下的文件复制到新设备的对应image目录下，恢复的日记即可正常显示配图，注意:你不能随意更改文件名。\n" +
            "Q7：横屏模式很奇怪啊。\n" +
            "A：首先无论是横屏还是分屏，我都没有特意去适配，我只考虑竖屏情况下的使用。因为横屏还要我" +
            "另外编写一个UI界面，我不干。所以我现在的做法就是无论你横屏/竖屏/分屏/小窗，只要显示不下，" +
            "就给你一个上下滑动的操作。嘻嘻，我就是这么菜还懒😛\n" +
            "Q8：我好像没有找到“检查更新”的按钮啊？\n" +
            "A：目前整个软件都没有接入网络服务，你只能在开源代码的网址中检查这个项目有没有发布新的release，" +
            "每个release都有对应的版本号，也就是下面“关于”中的“版本”。\n" +
            "Q9：为什么开源代码的地址打不开？\n" +
            "A：国外网站，多刷新几次。或者访问镜像版本[https://gitee.com/HibaraAi/Diary]\n" +
            "Q10：跨设备迁移记录具体如何操作？\n" +
            "A：1.旧设备中导出一个备份文件。" +
            "2.在新设备中恢复备份文件的记录。" +
            "3.将旧设备的图片资源(外部存储根目录/Hibara/Diary/image)复制到新设备的对应目录下。\n";
    public static final String STRING_HELP_7 = "\n七、写在最后\n" +
            "1.本软件不会盗取你任何数据，下面有开源代码可查。同样的，" +
            "如果你在使用本软件的过程中，产生无论何种形式的损失，都与本作者无关。\n" +
            "2.更多软件功能并没有在这里写明，你可以自行发现，例如可以长按日记文本进行复制等操作。\n" +
            "3.软件兼容性，1080p屏幕使用应该不会有什么问题，其他分辨率设备没试。Android 10以下的设备不能安装。\n" +
            "4.不要去系统中手动“删除数据”，“删除数据”相当于重装，这会删除软件所有的已存储数据。“清空缓存”可以，但是本应用提供了缓存清除功能。其实缓存归安卓系统自动管理，你大可不必自己去清空\n" +
            "\n八、本次更新内容\n" +
            "1. 修复Android11的兼容性\n" +
            "2. 新增感谢名单\n" +
            "3. 更换保存图片/视频的提示，长按标签进行复制\n" +
            "4. 单个日记的图片上限改为50张，视频改为8个\n";
    public static final String STRING_HELP = STRING_HELP_1+STRING_HELP_2+STRING_HELP_3+STRING_HELP_4+STRING_HELP_5+STRING_HELP_6+STRING_HELP_7;

    public static final String SHARE_PREFERENCES_HEAD_SRC = "headSrc";
    public static final String SHARE_PREFERENCES_USERNAME = "username";
    public static final String SHARE_PREFERENCES_MOTTO = "motto";
    public static final String SHARE_PREFERENCES_MAIN_IMG_BG = "main_bg_image";
    public static final String SHARE_PREFERENCES_PRIVATE_KEY = "private_key";
    public static final String SHARE_PREFERENCES_PUBLIC_KEY = "public_key";
    public static final String SHARE_PREFERENCES_DIARY_FONT_SIZE = "font_size";

    public static final int OPEN_ALBUM_TYPE_HEAD = 1;
    public static final int OPEN_ALBUM_TYPE_MAIN_BG = 2;
    public static final int OPEN_ALBUM_TYPE_KEEP_DIARY_ADD_PIC = 3;
    public static final int OPEN_ALBUM_TYPE_ADD_DAY_ADD_PIC = 4;

    public static final String BACKUP_ARGS_NAME_PRIVATE_KEY = "iudvgdsk";
    public static final String BACKUP_ARGS_NAME_PUBLIC_KEY = "ogfrlepr";
    public static final String BACKUP_ARGS_NAME_PIN_KEY = "epifuioew";
    public static final String BACKUP_ARGS_NAME_DATA_NAME = "wepityweio";
    public static final String BACKUP_ARGS_NAME_VERSION = "oqiteoqt";
    public static final String BACKUP_ARGS_NAME_UUID = "owqiioqghew";
    public static final String BACKUP_ARGS_NAME_ENCODE_UUID = "ewrewr";

    /**
     * 本应用在外部存储使用的路径名称
     */
    public static final String EXTERNAL_STORAGE_LOCATION = "/Hibara/Diary/";

    /**
     * 开启测试功能的测试码
     */
    public static final String TEST_FUN_KEY = "1212";

}
