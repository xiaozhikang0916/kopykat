package fp.serrano.transformative

import org.junit.jupiter.api.Test

class Tests {

  @Test
  fun `simple test`() {
    """
      |import fp.serrano.transformative
      |
      |@transformative data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.transform(age = { it + 1 })
      |val r = p2.age
      """.evals("r" to 2)
  }

  @Test
  fun `fails on non-data class`() {
    """
      |import fp.serrano.transformative
      |
      |@transformative class Person(val name: String, val age: Int)
      """.failsWith { it.contains("Only data classes can be annotated with @transformative") }
  }

  @Test
  fun `empty transform does nothing`() {
    """
      |import fp.serrano.transformative
      |
      |@transformative data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.transform()
      |val r = p2.age
      """.evals("r" to 1)
  }

  @Test
  fun `works on generic classes`() {
    """
      |import fp.serrano.transformative
      |
      |@transformative data class Person<A>(val name: String, val age: A)
      |
      |val p1: Person<Int> = Person("Alex", 1)
      |val p2 = p1.transform(age = { it + 1 })
      |val r = p2.age
      """.evals("r" to 2)
  }
}