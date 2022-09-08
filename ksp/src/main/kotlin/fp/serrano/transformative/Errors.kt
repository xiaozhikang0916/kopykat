package fp.serrano.transformative

import com.google.devtools.ksp.symbol.KSDeclaration

internal val KSDeclaration.notDataClassErrorMessage
  get() =
    """
      |${(qualifiedName ?: simpleName).asString()} cannot be annotated with @Transformative
      | ^
      |Only data classes can be annotated with @Transformative""".trimMargin()
