# Mapper Decorator

Mapper Decorator es una solución elegante diseñada para mapear clases en Kotlin. Fue creado específicamente para Room y, a diferencia de otros ORM, no admite relaciones.

## Características

- **@TableEntity**: Este decorador se utiliza para vincular una clase de entidad con una clase de datos. Le permite especificar qué clase de datos debe mapearse a una entidad específica.
- **@IgnoreField**: Este decorador se utiliza para indicar que un campo particular no debe ser incluido en el mapeo. Puede ser útil si tienes campos en la clase de origen que no tienen correspondencia en la clase de destino o si quieres excluir campos específicos por alguna razón.
- **@Mapper**: Este decorador se utiliza para indicar que una propiedad en particular debe ser mapeada. Permite una correspondencia precisa entre campos en clases de origen y destino.
- **@MapperName**: Este decorador se utiliza para cambiar el nombre del campo en el mapeo. Si tienes un campo con un nombre en la clase de origen y quieres que se mapee a un campo con un nombre diferente en la clase de destino, puedes utilizar esta anotación.
- **@PrimaryMapper**: Este decorador se utiliza para designar una entidad como la principal en un mapeo compuesto. Por ejemplo, en el mapeo de una persona con una dirección, la entidad persona podría ser la principal.

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

## Caso mas realista

Este documento proporciona una descripción detallada y un ejemplo concreto, lo cual debería ofrecer una comprensión completa de cómo funciona  Mapper Decorator. Por supuesto, puedes ajustar el contenido según tus necesidades y preferencias.


### Definir las Clases capa Data

Defina las clases entre las que desea mapear 
```kotlin

data class Persona(val id:String?=null,
                   val nombre:String?=null,
                   val correo:Correo?=null,
                   val edad:Int?=null,
                   val address: Address?=null
)

data class Correo(val id:String?=null,
                  val nombre:String?=null,
                  val fecha:String?=null,)

data class Address(val addressName:String?=null, val addresMulti:List<Int>?=null)

```
### Definir las Clases capa Entity

Defina las clases entre las que desea mapear, y utilice la anotación \`@TableEntity\` para vincularlas:


```kotlin
@TableEntity(Address::class)
data class AddressEntity (val addressName:String?=null, val addresMulti:List<Int>?=null)

@TableEntity(Correo::class)
data class CorreoEntity(val id:String?=null,
                        val nombre:String?=null,
                        val fecha:String?=null,)

@TableEntity(Persona::class)
data class PersonaEntity(
    val id: String? = null,
    val nombre: String? = null,
    @MapperName("edad") val edad_old:Int?=null,
    @Mapper val correo: CorreoEntity? = null,
    @IgnoreField val final_id:String?=null)


@TableEntity(Persona::class)
data class PersonawithAdress (
    @PrimaryMapper
    val personaEntity: PersonaEntity?=null,
    @Mapper
    @MapperName("address")
    val addressEntity: AddressEntity?=null)
```

### Generar los Mappers

El decorador generará automáticamente los mappers, permitiendo una conversión fácil entre las clases:

```kotlin

public object MapperAddressEntity {
  public fun toAddressEntityToAddress(addressentity: AddressEntity?): Address =
      com.grupoalv.mapper.`data`.Address(addressName=addressentity?.addressName
      ,addresMulti=addressentity?.addresMulti ,)

  public fun toAddressToAddressEntity(address: Address?): AddressEntity =
      com.grupoalv.mapper.entity.AddressEntity(addressName=address?.addressName
      ,addresMulti=address?.addresMulti ,)
}


public object MapperCorreoEntity {
    public fun toCorreoEntityToCorreo(correoentity: CorreoEntity?): Correo =
        com.grupoalv.mapper.`data`.Correo(id=correoentity?.id ,nombre=correoentity?.nombre
            ,fecha=correoentity?.fecha ,)

    public fun toCorreoToCorreoEntity(correo: Correo?): CorreoEntity =
        com.grupoalv.mapper.entity.CorreoEntity(id=correo?.id ,nombre=correo?.nombre
            ,fecha=correo?.fecha ,)
}

public object MapperPersonaEntity {
    public fun toPersonaEntityToPersona(personaentity: PersonaEntity?): Persona =
        com.grupoalv.mapper.`data`.Persona(id=personaentity?.id ,nombre=personaentity?.nombre
            ,edad=personaentity?.edad_old
            ,correo=com.grupoalv.mapper.entity.MapperCorreoEntity.toCorreoEntityToCorreo(personaentity?.correo),)

    public fun toPersonaToPersonaEntity(persona: Persona?): PersonaEntity =
        com.grupoalv.mapper.entity.PersonaEntity(id=persona?.id ,nombre=persona?.nombre
            ,correo=com.grupoalv.mapper.entity.MapperCorreoEntity.toCorreoToCorreoEntity(persona?.correo),edad_old=persona?.edad
            ,)
}


public object MapperPersonawithAdress {
    public fun toPersonawithAdressToPersona(personawithadress: PersonawithAdress?): Persona =
        com.grupoalv.mapper.`data`.Persona(address=com.grupoalv.mapper.entity.MapperAddressEntity
            .toAddressEntityToAddress(personawithadress?.addressEntity),id=personawithadress?.personaEntity?.id
            , nombre=personawithadress?.personaEntity?.nombre  ,
            correo=com.grupoalv.mapper.entity.MapperCorreoEntity.toCorreoEntityToCorreo(personawithadress?.personaEntity?.correo))

    public fun toPersonaToPersonawithAdress(persona: Persona?): PersonawithAdress =
        com.grupoalv.mapper.entity.PersonawithAdress(addressEntity=com.grupoalv.mapper
            .entity.MapperAddressEntity.toAddressToAddressEntity(persona?.address),
            personaEntity=com.grupoalv.mapper.entity.MapperPersonaEntity.toPersonaToPersonaEntity(persona),)
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
    implementation("com.github.FrancoAlv:AlvMapperDecoradorFranco:<YOUR_VERSION>") //1.0.1
    kapt("com.github.FrancoAlv:AlvMapperFranco:<YOUR_VERSION>") //1.0.2
    
}
```


## Conclusión

Mapper Decorator elimina la necesidad de escribir manualmente el código repetitivo y propenso a errores para mapear entre clases. Proporciona una forma declarativa y eficiente de definir cómo se deben traducir las clases entre diferentes capas o contextos.

Este documento proporciona una descripción detallada y un ejemplo concreto, lo cual debería ofrecer una comprensión completa de cómo funciona Alv Mapper Decorator. Puedes ajustar el contenido según tus necesidades y preferencias, y comenzar a utilizar esta poderosa herramienta en tu proyecto hoy mismo.


## Contribuir

Si tienes ideas o sugerencias, ¡nos encantaría escucharlas!

## Licencia

MIT
