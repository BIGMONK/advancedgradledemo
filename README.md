#基础知识

首先我们要知道gradle中有一个功能叫做变体「productflavors」，这是来为APP设置不同的打包配置，以实现多渠道打包的一种方案。基本形式如下：

android {
  ...
  buildTypes {
     debug {
          ...
        }
        qa {
          ...
        }
        release {
          ...
         }
  }
  productFlavors {
      baidu{}
      _360{}
      yingyongbao{}
  }
这样的话最后打包的时候就可以生成9种包：

· baiduDebug
· baiduQa
· baiduRelease
· _360Debug
· _360Qa
· _360Release
· yingyongbaoDebug
· yingyongbaoQa
· yingyongbaoRelease
在Android Studio左下角可以找到并在每次build的时候选择不同种类的包

![](http://upload-images.jianshu.io/upload_images/4287985-c5e3af1a7f908f3c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
注意名称不能用数字，所以我这里没有用360。

#实现分渠道配置

##配置java变量

在gradle中有一个功能叫「buildConfigField」，可以在系统的buildconfig中设置一个值。如下：

buildConfigField 'boolean', 'ISB', 'true'
这样就可以在app中使用boolean类型的变量BuildConfig.ISB=true
这里也可以建立一个string值，如下：

buildConfigField 'String', 'val', '"content"'
在java代码中使用Build.val 可以使用这个量，值为content
![](http://upload-images.jianshu.io/upload_images/4287985-d03a3a2623021149.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
##配置manifest变量

很多第三方sdk喜欢在manifest中配置appkey等，可以在gradle中使用：

 manifestPlaceholders = [UMENG_CHANNEL: "0",
                                    UMENG_APPKEY : "123456789"]
然后在manifest中配置

<meta-data
            android:name="UMENG_APPKEY"
            android:value="${UMENG_APPKEY}" />
        <meta-data
            android:name="UMENG_CHANNEL"
            android:value="${UMENG_CHANNEL}" />
就可以实现。

##配置包名

在gradle中包名用applicationId代表。还可以使用applicationIdSuffix在后面加一个后缀。
比如原包名是

applicationId "com.a.b"
使用

applicationIdSuffix ".c"
最终打包以后的包名就是com.a.b.c。

##配置版本号

是的，versionCode和versionName也可以自己配置，毕竟他们是一个变量~

###实战

前面说了这么多可配置的项，其实我还有一点没有说，那就是这一切都是可以在productFlavors和buildTypes配置的！
所以我们可以利用这点实现更高级的需求。

###日志开关

buildTypes {
        debug {
            buildConfigField 'boolean', 'PROXY', 'true'
        }
        qa {
            buildConfigField 'boolean', 'PROXY', 'true'
        }
        release {
            buildConfigField 'boolean', 'PROXY', 'false'
        }
在buildTypes中配置，release配置为false 其他为true。便于调试

###api环境地址

一般情况下后端的api地址区别就在于域名，所以在debug qa release可以使用不同的域名

buildTypes {
        debug {
            buildConfigField 'String', 'API_HOST', '"http://dev.example.com/"'
        }
        qa {
            buildConfigField 'String', 'API_HOST', '"http://qa.example.com/"'
        }
        release {
            buildConfigField 'String', 'API_HOST', '"https://example.com/v1/"'
        }
    }
###不同的包名 版本号

这些都是可以使用自带的属性进行配置。在不同的buildTypes中赋值不同即可。

productFlavors {
        baidu {
            applicationId "com.janus.baidu.advancedgradledemo"
            versionCode 1
            versionName "1.0.0"
        }
        _360 {
            applicationId "com.janus.360.advancedgradledemo"
            versionCode 2
            versionName "1.0.2"
        }
        yingyongbao {
            applicationId "com.janus.yingyongbao.advancedgradledemo"
            versionCode 3
            versionName "1.0.3"
        }
    }
##更高级的实战：不同的APP名称 图标 UI等

相信大家都遇到过这种情况，公司做出了一个统一框架，只有业务逻辑是不同的，其他的都一样。其中业务逻辑是随着OEM厂商而变化的，同时UI 图标 描述语等也是不同的。一般的情况下就只能分开成好几个项目了，不过在这里可以用一种方法实现。
如果有2个oem分别叫做oea oeb
我们可以使用productFlavors做出2个flavor

productFlavors {
        oea {
        }
        oeb {
        }
    }
###APP名称

名称是在manifest的application节点下的label属性设置。我们可以使用manifestPlaceholders进行设置
manifest中：

<application
        android:label="${APP_NAME}"
        tools:replace="android:label"
注意：动态替换label需要添加一行 tools:replace="android:label"
gradle中：

productFlavors {
        oea {
            manifestPlaceholders = [APP_NAME: "APP_OEA"]
        }
        oeb {
            manifestPlaceholders = [APP_NAME: "APP_OEB"]
        }
    }
这样打不同的包就会出现不同的名称。

###APP图标、UI

其实还有一个知识没有介绍，留到这里是为了配合这个需求。那就是分module进行依赖。
大家都知道图标 UI主题色 图片资源这样的东西是放在res中的，我们也可以使用多个module来分开放置。但是如何把不同的oe和不同的module关联起来呢？
现在我们有2个oe：oea oeb

productFlavors {
        oea {
        }
        oeb {
        }
    }
然后新建两个module分别叫oea oeb

![](http://upload-images.jianshu.io/upload_images/4287985-8ce3c2e1f8416880.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
###关键代码：

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    oeaCompile project(':oea')
    oebCompile project(':oeb')
}
xxCompile 代表使用productFlavors的名称（这里就是oea），将flavor和module关联起来。
也就是
打oea包的时候依赖oea这个module
打oeb包的时候依赖oeb这个module

好了现在可以在不同的module中放置同名的资源，然后打不同包的时候就可以自动切换成不同的资源依赖了！
比如我们的APP图标，声明在manifest中

<application
        android:icon="@mipmap/ic_launcher"
那么我们就在两个module中放入同名的图标文件，注意在主module（app）中反而要删除。
在打包oea的时候只会依赖oea，只有oea里面有图标。
在打包oeb的时候只会依赖oeb，只有oeb里面有图标。
这样图标就能实现不同的了。

![](http://upload-images.jianshu.io/upload_images/4287985-a413488635800f10.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
最终效果：


![](http://upload-images.jianshu.io/upload_images/4287985-44a5186f9f91c737.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
##UI

同样使用上面的方法，可以实现整套图片素材、颜色、string中的文字描述等等的全部替换。
甚至还可以根据不同的oem使用不同的业务逻辑。
具体做法：
在productFlavors中声明一个变量，根据这个变量来更改业务逻辑。
gradle中：

productFlavors {
        oea {
            buildConfigField 'String', 'OEM', '"OEA"'
        }
        oeb {
            buildConfigField 'String', 'OEM', '"OEB"'
        }
    }
java中：

switch (BuildConfig.OEM) {
            case "OEA":
                break;
            case "OEB":
                break;
        }

