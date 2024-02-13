package cz.mendelu.pef.xvlastni.annotationsprocessor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class PrintMessageProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation(PrintMessage::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .forEach { classDeclaration ->
                val packageName = classDeclaration.packageName.asString()
                val className = classDeclaration.simpleName.asString()
                val message = classDeclaration.annotations.first {
                    it.shortName.asString() == "PrintMessage"
                }.arguments.first().value.toString()

                val fileContent = """
                    package $packageName
                    
                    fun printMessageFor$className() {
                        println("$message")
                    }
                """.trimIndent()

                codeGenerator.createNewFile(
                    dependencies = Dependencies(aggregating = false),
                    packageName = packageName,
                    fileName = "PrintMessageFor$className"
                ).writer().use {
                    it.write(fileContent)
                }
            }

        return emptyList()
    }
}