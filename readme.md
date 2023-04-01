# 反射工具

由于做一些框架或是组件经常用到反射，所以整理出这个工具，用来简化一些操作。

可用方法：

- 获取类的所有属性，包括父类
- 获取类的所有方法，包括父类
- 获取类的泛型标记表
  - 例如`<T, V>`对应的真实类型
- 获取参数类型的泛型标记表
   - 包括类对应的类型与其可能包括的泛型真实类型，就像`List<String>`
- 获取类的泛型信息
   - 包括类对应的类型与其可能包括的泛型真实类型，就像`List<String>`
- 获取属性的泛型信息
   - 包括属性对应的类型与其可能包括的泛型真实类型
- 获取方法的泛型信息
   - 包括方法的参数列表与返回值对应的类型与其可能包括的泛型真实类型
- 获取类的泛型转换类信息
   - 包括类自身的泛型信息，与其所有属性的泛型信息和方法泛型信息
- 从Lambda表达式中获取属性
  - 例如从`Person::getName`获取到`name`属性
- 通过参数匹配实例化类
  - 例如`ReflectUtil.newInstance(Person, "小明")`会通过`Person(String)`构造器来构造出`person`的实例对象
- 对对象属性进行`set`个`get`

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
