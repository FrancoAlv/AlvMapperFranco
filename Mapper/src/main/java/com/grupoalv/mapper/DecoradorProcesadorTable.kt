package com.grupoalv.mapper

import com.google.auto.service.AutoService
import com.grupoalv.decorador.IgnoreField
import com.grupoalv.decorador.Mapper
import com.grupoalv.decorador.MapperName
import com.grupoalv.decorador.PrimaryMapper
import com.grupoalv.decorador.TableEntity
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

@AutoService(Processor::class)
class DecoradorProcesadorTable : AbstractProcessor() {

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    private val nombreBase = "Mapper"

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(TableEntity::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment,
    ): Boolean {
        roundEnv.getElementsAnnotatedWith(TableEntity::class.java).forEach {
            if (it.kind != ElementKind.CLASS) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Only classes can be annotated"
                )
                return true
            }
            processAnnotation(it)
        }
        return false
    }

    private fun processAnnotation(element: Element) {
        val className = element.simpleName.toString()
        val pack = processingEnv.elementUtils.getPackageOf(element).toString()
        val fileName = "$nombreBase$className"
        val fileBuilder = FileSpec.builder(pack, fileName)
        val classBuilder = TypeSpec.objectBuilder(fileName)
        /* for (enclosed in element.enclosedElements) {
             if (enclosed.kind == ElementKind.FIELD) {
                 // validateoffield(enclosed, classBuilder)
             }
         }*/
        createofFunctionofMapper(element, classBuilder)
        val file = fileBuilder.addType(classBuilder.build()).build()
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        file.writeTo(File(kaptKotlinGeneratedDir))
    }


    private fun createofFunctionofMapper(element: Element, classBuilder: TypeSpec.Builder) {
        val className = element.simpleName.toString()
        val decorador = element.getAnnotation(TableEntity::class.java)
        if (decorador == null) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "No esta decorado el elemento"
            )
        }
        val classToMapMirror: TypeMirror
        try {
            decorador.classToMap // Esta línea debería lanzar MirroredTypeException
            throw RuntimeException()
        } catch (mte: MirroredTypeException) {
            classToMapMirror = mte.typeMirror
        }
        val classtomapelement = processingEnv.typeUtils.asElement(classToMapMirror)
        val nombredevaribale = element.simpleName.toString().lowercase()
        classBuilder.addFunction(
            FunSpec.builder("to${className}To${classtomapelement.simpleName}")
                .addParameter(
                    ParameterSpec.builder(
                        nombredevaribale,
                        element.asType().asTypeName().copy(nullable = true)
                    ).build()
                )
                .returns(classtomapelement.asType().asTypeName().copy(nullable = false))
                .addStatement(
                    generateStringmapper(
                        classtomapelement,
                        element,
                        nombredevaribale,
                        true
                    )
                )
                .build()
        )
        val nombredevaribalerever = classtomapelement.simpleName.toString().lowercase()
        classBuilder.addFunction(
            FunSpec.builder("to${classtomapelement.simpleName}To${className}")
                .addParameter(
                    ParameterSpec.builder(
                        nombredevaribalerever,
                        classtomapelement.asType().asTypeName().copy(nullable = true)
                    ).build()
                )
                .returns(element.asType().asTypeName().copy(nullable = false))
                .addStatement(
                    generateStringmapper(
                        element,
                        classtomapelement,
                        nombredevaribalerever,
                        false,
                    )
                )
                .build()
        )


    }

    private fun validatefieldsofclass(it: Element): Boolean =
        !it.modifiers.contains(Modifier.STATIC) && !it.modifiers.contains(
            Modifier.NATIVE
        ) && it.kind == ElementKind.FIELD && it.getAnnotation(IgnoreField::class.java) == null


    private fun generateStringmapper(
        elementpadre: Element,
        elementparamter: Element,
        nombredevaribale: String,
        istomapper: Boolean = true,
    ): String {
        val enclosed = elementparamter.enclosedElements
        if (enclosed.isEmpty()) {
            return ""
        }
        val result = java.lang.StringBuilder()
        var primarimapper = 0
        val input = java.lang.StringBuilder()
        result.append("return ${elementpadre.asType().asTypeName().copy(nullable = false)}(")
        enclosed.filter(::validatefieldsofclass).forEach { element ->

            val elementsindecorador =
                elementpadre.enclosedElements.asIterable().filter(::validatefieldsofclass)
                    .find { it2 ->
                        validatevalueMapper(
                            it2,
                            element,
                            istomapper
                        ) || it2.simpleName == element.simpleName
                    } ?: return@forEach

            if (element.asType().toString() == elementsindecorador.asType().toString()) {

                input.append("${elementsindecorador.simpleName}=${nombredevaribale}?.${element.simpleName}")
                input.append("${defaultValueForTypeName(element.asType().asTypeName())} ")
                input.append(",")
                return@forEach
            }
            val decorador = element.getAnnotation(Mapper::class.java)
            if (decorador == null && istomapper) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "No puedes tener un clase con un objeto de diferentes tipos sin el decorador mapper ${elementparamter.simpleName}"
                )
                return@forEach
            }
            val elementnombretype = processingEnv.typeUtils.asElement(element.asType())
            val elementsindecoradornombretype =
                processingEnv.typeUtils.asElement(elementsindecorador.asType())
            if (validateelemtensIsArray(elementsindecorador, element)) {
                //("${elementmmaper.simpleName}=${nombredevaribale}?.${elementsindecorador?.simpleName},"
                val typeMirrorpadre = elementsindecorador.asType() as DeclaredType
                val typeMirrormapper = element.asType() as DeclaredType
                val listTypepadre = typeMirrorpadre.typeArguments[0]
                val listTypemapper = typeMirrormapper.typeArguments[0]
                val elemetlistpadre = processingEnv.typeUtils.asElement(listTypepadre)
                val elemetlistmapper = processingEnv.typeUtils.asElement(listTypemapper)

                input.append(
                    "${elementsindecorador.simpleName}=${nombredevaribale}?.${element.simpleName}?.map{ ${
                        findimportofElemet(
                            if (istomapper) elemetlistmapper else elemetlistpadre
                        )
                    }.$nombreBase${if (istomapper) elemetlistmapper.simpleName else elemetlistpadre.simpleName}.to${elemetlistmapper.simpleName}To${elemetlistpadre.simpleName}(it) }?.toMutableList(),"
                )
                return@forEach
            }
            // en caso no sea array


            input.append(
                "${elementsindecorador.simpleName}=${findimportofElemet(if (istomapper) elementnombretype else elementsindecoradornombretype)}.$nombreBase${if (istomapper) elementnombretype.simpleName else elementsindecoradornombretype.simpleName}.to${elementnombretype.simpleName}To${elementsindecoradornombretype.simpleName}(${nombredevaribale}?.${
                    element.simpleName
                }),"
            )

        }
        if (!istomapper) {
            elementpadre.enclosedElements.asIterable()
                .filter { element -> element.getAnnotation(PrimaryMapper::class.java) != null }
                .forEach { element ->
                    val elementnombretype = processingEnv.typeUtils.asElement(element.asType())
                    input.append(
                        "${element.simpleName}=${findimportofElemet(elementnombretype)}.$nombreBase${elementnombretype.simpleName}.to${elementparamter.simpleName}To${elementnombretype.simpleName}($nombredevaribale),"
                    )
                }
        }


        enclosed.filter(::validatefieldsofclass)
            .filter { element -> element.getAnnotation(PrimaryMapper::class.java) != null }
            .forEach { element ->

                if (primarimapper > 1) {
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "No puedes tener dos mapper primarios"
                    )
                    throw RuntimeException()
                }
                primarimapper++
                val mapperstring = bajandonivelbymapper(
                    elementpadre,
                    element,
                    "${nombredevaribale}?.${element.simpleName}"
                )
                val listadoinput = input.split(",")
                val listadomapperstring = mapperstring.split(",")
                val resultdevalidacion = listadomapperstring.filter { mapper ->
                    listadoinput.none { input ->
                        input.split("=").firstOrNull() == mapper.split("=").firstOrNull()
                    }
                }.joinToString { it }
                input.append(resultdevalidacion)

            }
        result.append(input.toString())
        val value = result.append(")").toString()
        return value
    }

    fun defaultValueForTypeName(typeName: TypeName): String? {
        return when (typeName) {
            INT -> "?: 0"
            LONG -> "?: 0L"
            SHORT -> "?: 0"
            BYTE -> "?: 0"
            FLOAT -> "?: 0f"
            DOUBLE -> "?: 0.0"
            BOOLEAN -> "?: false"
            CHAR -> "?: '\u0000'"
            STRING -> "\" \""
            else -> ""
        }
    }

    private fun getvalueofdecoradororElemet(element: Element): String {
        val decorador = element.getAnnotation(MapperName::class.java)
            ?: return element.simpleName.toString()
        return decorador.nombreValue.ifEmpty { element.simpleName.toString() }
    }

    private fun validatevalueMapper(it2: Element?, element: Element, istomapper: Boolean): Boolean {
        if (istomapper) {
            val decorador = element.getAnnotation(MapperName::class.java) ?: return false
            return decorador.nombreValue == it2?.simpleName.toString()
        }
        val decorador = it2?.getAnnotation(MapperName::class.java) ?: return false
        return decorador.nombreValue == element.simpleName.toString()
    }

    private fun bajandonivelbymapper(
        elementpadre: Element,
        element: Element,
        nombredevaribale: String,
        istomapper: Boolean = true,
    ): String {
        val result = java.lang.StringBuilder()

        val fieldElement = processingEnv.typeUtils.asElement(element.asType())
        fieldElement.enclosedElements.filter(::validatefieldsofclass).forEach { elementhijo ->
            val elementsindecorador =
                elementpadre.enclosedElements.asIterable().filter(::validatefieldsofclass)
                    .find { it2 -> it2.simpleName == elementhijo.simpleName } ?: return@forEach
            if (elementhijo.asType()
                    .toString() == elementsindecorador.asType().toString()
            ) {

                result.append("${elementhijo.simpleName}=${nombredevaribale}?.${elementsindecorador.simpleName}")
                result.append(
                    " ${defaultValueForTypeName(
                        elementhijo.asType().asTypeName())
                    } "
                )
                result.append(",")
                return@forEach
            }
            val decorador = elementhijo.getAnnotation(Mapper::class.java)
            if (decorador == null && istomapper) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "No puedes tener un clase con un objeto de diferentes tipos sin el decorador mapper"
                )
                return@forEach
            }
            val elementnombretype = processingEnv.typeUtils.asElement(elementhijo.asType())
            val elementsindecoradornombretype =
                processingEnv.typeUtils.asElement(elementsindecorador.asType())
            if (validateelemtensIsArray(elementsindecorador, elementhijo)) {
                //("${elementmmaper.simpleName}=${nombredevaribale}?.${elementsindecorador?.simpleName},"
                val typeMirrorpadre = elementsindecorador.asType() as DeclaredType
                val typeMirrormapper = elementhijo.asType() as DeclaredType
                val listTypepadre = typeMirrorpadre.typeArguments[0]
                val listTypemapper = typeMirrormapper.typeArguments[0]
                val elemetlistpadre = processingEnv.typeUtils.asElement(listTypepadre)
                val elemetlistmapper = processingEnv.typeUtils.asElement(listTypemapper)

                result.append(
                    "${elementhijo.simpleName}=${nombredevaribale}?.${elementsindecorador.simpleName}?.map{ ${
                        findimportofElemet(
                            if (istomapper) elemetlistmapper else elemetlistpadre
                        )
                    }.$nombreBase${if (istomapper) elemetlistmapper.simpleName else elemetlistpadre.simpleName}.to${elemetlistmapper.simpleName}To${elemetlistpadre.simpleName}(it) }?.toMutableList(),"
                )
                return@forEach
            }

            // en caso no sea array

            result.append("${elementhijo.simpleName}=${findimportofElemet(if (istomapper) elementnombretype else elementsindecoradornombretype)}.$nombreBase${if (istomapper) elementnombretype.simpleName else elementsindecoradornombretype.simpleName}.to${elementnombretype.simpleName}To${elementsindecoradornombretype.simpleName}(${nombredevaribale}?.${elementsindecorador.simpleName}),")

        }

        return result.toString()
    }


    private fun findimportofElemet(element: Element): String {
        return element.asType().asTypeName().toString().split(".").toMutableList().apply {
            removeLast()
        }.joinToString(separator = ".") { it }
    }

    private fun validatekinddeclare(vararg fisrtkind: TypeKind): Boolean {
        return fisrtkind.any { it == TypeKind.DECLARED }
    }

    private fun validateelemtensIsArray(vararg element: Element): Boolean {
        return element.any {
            if (!validatekinddeclare(it.asType().kind)) {
                return@any false
            }
            val typeMirrorpadre = it.asType() as DeclaredType
            (typeMirrorpadre.asElement().kind == ElementKind.INTERFACE &&
                    ((typeMirrorpadre.asElement() as TypeElement).qualifiedName.toString() == "java.util.List" ||
                            (typeMirrorpadre.asElement() as TypeElement).qualifiedName.toString() == "java.util.ArrayList"))
        }
    }


    private fun validateoffield(
        enclosed: Element,
        classBuilder: TypeSpec.Builder,
    ) {
        val decorador = enclosed.getAnnotation(Mapper::class.java)
        if (decorador == null) {
            createofcodigo(classBuilder, enclosed)
            return
        }


        val fieldType = enclosed.asType()
        val fieldElement = processingEnv.typeUtils.asElement(fieldType)
        for (infield in fieldElement.enclosedElements) {
            if (infield.kind == ElementKind.FIELD && infield.getAnnotation(IgnoreField::class.java) == null) {
                createofcodigo(classBuilder, infield)
            }
        }


    }


    private fun createofcodigo(
        classBuilder: TypeSpec.Builder,
        enclosed: Element,
    ) {
        classBuilder.addProperty(
            PropertySpec.builder(
                enclosed.simpleName.toString(),
                enclosed.asType().asTypeName().copy(nullable = true),
                KModifier.PRIVATE
            ).mutable(true)
                .initializer("null")
                .build()
        )
        classBuilder.addFunction(
            FunSpec.builder("get${enclosed.simpleName}")
                .returns(enclosed.asType().asTypeName().copy(nullable = true))
                .addStatement("return ${enclosed.simpleName}")
                .build()
        )
        classBuilder.addFunction(
            FunSpec.builder("set${enclosed.simpleName}")
                .addParameter(
                    ParameterSpec.builder(
                        "${enclosed.simpleName}",
                        enclosed.asType().asTypeName().copy(nullable = true)
                    ).build()
                )
                .addStatement("this.${enclosed.simpleName} = ${enclosed.simpleName}")
                .build()
        )
    }


}