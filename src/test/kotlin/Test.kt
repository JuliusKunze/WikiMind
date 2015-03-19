import org.jetbrains.spek.api.*

class SpekSpecs : Spek() {{
    given("something") {
        on("an event") {
            it("should work") {
                main(array())
                println("Does this work?")
            }
        }
    }
}}