package fp.serrano.transformative

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

public class TransformativeProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
    TransformativeProcessor(
      codegen = environment.codeGenerator,
      logger = environment.logger,
      transform = environment.options.parseBoolOrTrue("transform"),
      mutableCopy = environment.options.parseBoolOrTrue("mutableCopy")
    )
}

private fun Map<String, String>.parseBoolOrTrue(key: String) =
  this[key]?.lowercase()?.toBooleanStrictOrNull() ?: true
