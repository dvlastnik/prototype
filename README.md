# compose-rapid-prototyping

## Overview
This library is a Kotlin-based library designed for Android developers. It utilizes Jetpack Compose to streamline and accelerate the UI development process. This project focuses on enabling rapid prototyping, helping developers quickly test their database or server communication.

### Contacts
 - **Author**: David Vlastník
 - **Supervisor**: Ing. Jaromír Landa, Ph.D

## Features

### Efficient UI Prototyping
 - Leverage the power of Jetpack Compose for faster UI development.

### Kotlin Integration
 - Seamlessly integrates with Kotlin, enhancing coding efficiency and readability.

### User-Friendly
 - Designed for ease of use, suitable for beginners and experienced developers alike.

## Getting Started

### Prerequisites
In order to make this library work with your app you have to use these libraries:
 - Room - for database
 - Retrofit - for server communication
 - Hilt - for dependency injection

Add KSP to your project level build.gradle:
```build.gradle.kts
    classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:<ksp_version>")    
```

Add KSP to your app module build.gradle:
```build.gradle.kts
    id("com.google.devtools.ksp") version "<ksp_version>"
```

Because library is stored at Jitpack.io, you have to add this in your 'settings.gradle.kts':
```settings.gradle.kts
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		mavenCentral()
		maven { url = uri("https://jitpack.io") }
	}
}
```

Finally add dependency of this library:
```build.gradle.kts
dependencies {
    implementation("com.github.dvlastnik:prototype:v1.0.2")
    ksp("com.github.dvlastnik:prototype:v1.0.2")
}
```

### Example use (for database)
#### 1. Annotate your data class
 - Annotate with '@RapidPrototype'
 - Available types are 'RapidPrototype.API' and 'RapidPrototypeType.DATABASE'
 - isList = false is available only for API, because why database without list?

```kotlin
@RapidPrototype(isList = true, type = RapidPrototypeType.DATABASE)
@Entity(tableName = "activities")
data class Activity(
    @ColumnInfo(name = "key")
    val key: String,
    @ColumnInfo(name = "accessibility")
    val accessibility: Double,
    @ColumnInfo(name = "activity")
    val activity: String,
    @ColumnInfo(name = "link")
    val link: String,
    @ColumnInfo(name = "participants")
    val participants: Int,
    @ColumnInfo(name = "price")
    val price: Double,
    @ColumnInfo(name = "type")
    val type: String
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long? = null
}
```

#### 2. Annotate your Repository
 - Use repository pattern
 - Annotate it with '@RapidPrototypeRepository'
 - Annotate your repository functions with '@RapidPrototypeFunction'
   - Its parameter is 'type'
   - Available types are these:
     - RapidPrototypeFunctionType.SELECT - for SELECT from database or GET from server
     - RapidPrototypeFunctionType.INSERT - for INSERT to database or POST to server
     - RapidPrototypeFunctionType.DELETE - for DELETE from database or DELETE from server

```kotlin
@RapidPrototypeRepository
interface IAppRepository {

    @RapidPrototypeFunction(type = RapidPrototypeFunctionType.SELECT)
    fun getSavedActivites(): Flow<List<Activity>>

    @RapidPrototypeFunction(type = RapidPrototypeFunctionType.INSERT)
    suspend fun insertActivity(activity: Activity): Long

    @RapidPrototypeFunction(type = RapidPrototypeFunctionType.DELETE)
    suspend fun deleteActivity(activity: Activity)
}
```

#### 3. Build your app
 - After build, screen is generated, can be found at 'build/generated/ksp/<debug or release>'

#### 4. Use screen in MainActivity
 - Add generated screen to main activity
 - If you annotated INSERT function, you have to fill 'whatToInsert' parameter, basically it defines default Object that will be inserted everytime you press add button

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeRapidPrototypingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ActivityScreen(
                        whatToInsert = Activity(
                            key = "000", 
                            accessibility = 1.0, 
                            activity = "programovat", 
                            link = "", participants = 1, 
                            price = 1.0, 
                            type = "programming"
                        )
                    )
                }
            }
        }
    }
}
```
