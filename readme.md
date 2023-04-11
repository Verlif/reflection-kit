# 反射工具

由于做一些框架或是组件经常用到反射，所以整理出这个工具，用来简化一些操作。

这里并不涉及反射的基本方法，并且所有的方法都是**静态方法**，可以直接使用。

- 属性
  - 获取对象属性值
  - 设置对象属性值
  - 获取类的所有属性（包括继承属性）
  - 获取属性的泛型信息
- 方法
  - 获取类的所有方法（包括继承方法）
  - 获取类的泛型信息
- 类
  - 获取类的泛型信息
  - 通过参数构造类的对象

## 依赖

1. 添加Jitpack仓库源

   maven

    ```xml
    <repositories>
       <repository>
           <id>jitpack.io</id>
           <url>https://jitpack.io</url>
       </repository>
    </repositories>
    ```

   Gradle

    ```text
    allprojects {
      repositories {
          maven { url 'https://jitpack.io' }
      }
    }
    ```

2. 添加依赖

   __lastVersion__ [![reflection-kit](https://jitpack.io/v/Verlif/reflection-kit.svg)](https://jitpack.io/#Verlif/reflection-kit)

   maven

   ```xml
      <dependencies>
          <dependency>
              <groupId>com.github.Verlif</groupId>
              <artifactId>reflection-kit</artifactId>
              <version>lastVersion</version>
          </dependency>
      </dependencies>
   ```

   Gradle

   ```text
   dependencies {
     implementation 'com.github.Verlif:reflection-kit:lastVersion'
   }
   ```
