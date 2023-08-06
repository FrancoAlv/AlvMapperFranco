# Alv Mapper Decorator

Alv Mapper Decorator es una poderosa herramienta que permite generar mappers entre diferentes clases de una manera eficiente y elegante.

## Características

- **@TableEntity**: Anotación para identificar la clase principal y la clase a la que se mapeará.
- **@IgnoreField**: Ignora un campo específico en el mapeo.
- **@Mapper**: Define una función personalizada para el mapeo.
- **@MapperName**: Establece un nombre personalizado para el mapper.
- **@PrimaryMapper**: Define el mapper principal en caso de múltiples mappers.

## Uso

### Definir las Clases

Defina las clases entre las que desea mapear, y utilice la anotación \`@TableEntity\` para vincularlas:

\`\`\`kotlin
import com.grupoalv.decorador.TableEntity

@TableEntity(MapperTwo::class)
data class Mapperone(var id: String? = null)

data class MapperTwo(var id: String? = null)
\`\`\`

### Generar los Mappers

El decorador generará automáticamente los mappers, permitiendo una conversión fácil entre las clases:

\`\`\`kotlin
public object MapperMapperone {
public fun toMapperoneToMapperTwo(mapperone: Mapperone?): MapperTwo =
com.grupoalv.mapper.MapperTwo(id=mapperone?.id ,)

public fun toMapperTwoToMapperone(mappertwo: MapperTwo?): Mapperone =
com.grupoalv.mapper.Mapperone(id=mappertwo?.id ,)
}
\`\`\`

### Utilizar las Anotaciones Adicionales

Utilice las anotaciones adicionales como \`@IgnoreField\`, \`@Mapper\`, \`@MapperName\`, y \`@PrimaryMapper\` para personalizar el comportamiento del mapeo según sus necesidades.

## Instalación

Incluya las dependencias necesarias en su archivo \`build.gradle\`:

\`\`\`groovy

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
\`\`\`

## Contribuir

Si tienes ideas o sugerencias, ¡nos encantaría escucharlas!

## Licencia

MIT
