# Mapper Decorator

Mapper Decorator es una solución elegante diseñada para mapear clases en Kotlin. Fue creado específicamente para Room y, a diferencia de otros ORM, no admite relaciones.

## Características

- **@TableEntity**: Anotación para identificar la clase principal y la clase a la que se mapeará.
- **@IgnoreField**: Ignora un campo específico en el mapeo.
- **@Mapper**: Define una función personalizada para el mapeo.
- **@MapperName**: Establece un nombre personalizado para el mapper.
- **@PrimaryMapper**: Define el mapper principal en caso de múltiples mappers.

## Uso

### Definir las Clases

Defina las clases entre las que desea mapear, y utilice la anotación \`@TableEntity\` para vincularlas:

```kotlin
import com.grupoalv.decorador.TableEntity

@TableEntity(MapperTwo::class)
data class Mapperone(var id: String? = null)

data class MapperTwo(var id: String? = null)
```

### Generar los Mappers

El decorador generará automáticamente los mappers, permitiendo una conversión fácil entre las clases:

```kotlin
public object MapperMapperone {
public fun toMapperoneToMapperTwo(mapperone: Mapperone?): MapperTwo =
com.grupoalv.mapper.MapperTwo(id=mapperone?.id ,)

public fun toMapperTwoToMapperone(mappertwo: MapperTwo?): Mapperone =
com.grupoalv.mapper.Mapperone(id=mappertwo?.id ,)
}

```

### Caso mas realista
```kotlin
data class Persona(val id:String?=null,
                   val nombre:String?=null,
                   val correo:Correo?=null,
                   val edad:Int?=null,)

data class Correo(val id:String?=null,
                  val nombre:String?=null,
                  val fecha:String?=null,)

@TableEntity(Correo::class)
data class CorreoEntity(val id:String?=null,
                        val nombre:String?=null,
                        val fecha:String?=null,)

@TableEntity(Persona::class)
data class PersonaEntity(
    val id: String? = null,
    val nombre: String? = null,
    @MapperName("edad") val edad_old:Int?=null,
    @Mapper val correo: Correo? = null,
    @IgnoreField val final_id:String?=null,
)

public object MapperPersonaEntity {
    public fun toPersonaEntityToPersona(personaentity: PersonaEntity?): Persona =
        com.grupoalv.mapper.data.Persona(
            id = personaentity?.id, nombre = personaentity?.nombre,
            edad = personaentity?.edad_old, correo = personaentity?.correo,
        )

    public fun toPersonaToPersonaEntity(persona: Persona?): PersonaEntity =
        com.grupoalv.mapper.entity.PersonaEntity(
            id = persona?.id, nombre = persona?.nombre,
            correo = persona?.correo, edad_old = persona?.edad,
        )
}

public object MapperCorreoEntity {
    public fun toCorreoEntityToCorreo(correoentity: CorreoEntity?): Correo =
        com.grupoalv.mapper.data.Correo(id=correoentity?.id ,nombre=correoentity?.nombre
            ,fecha=correoentity?.fecha ,)

    public fun toCorreoToCorreoEntity(correo: Correo?): CorreoEntity =
        com.grupoalv.mapper.entity.CorreoEntity(id=correo?.id ,nombre=correo?.nombre
            ,fecha=correo?.fecha ,)
}
```

### Utilizar las Anotaciones Adicionales

Utilice las anotaciones adicionales como \`@IgnoreField\`, \`@Mapper\`, \`@MapperName\`, y \`@PrimaryMapper\` para personalizar el comportamiento del mapeo según sus necesidades.

## Instalación

Incluya las dependencias necesarias en su archivo \`build.gradle\`:

```groovy

plugins {
id("org.jetbrains.kotlin.kapt")
}
android{
sourceSets {
getByName("main") {
java.srcDir("${buildDir.absolutePath}/generated/source/kaptKotlin/")
}
}
}

dependencies {
implementation("com.github.FrancoAlv:AlvMapperDecoradorFranco:1.0.1")
kapt("com.github.FrancoAlv:AlvMapperFranco:1.0.2")
}
```

## Contribuir

Si tienes ideas o sugerencias, ¡nos encantaría escucharlas!

## Licencia

MIT
