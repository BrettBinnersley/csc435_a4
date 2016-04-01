package main

import (
	"fmt"
)

func main() {
    fmt.Println("hello world")
    var x int
    x = factorial(1)
    fmt.Println("factorial(1) =", x)
    x = factorial(6)
    fmt.Println("factorial(6) =", x)
}

func factorial( k int ) int {
    if k < 2 {
        return 1
    }
    return k*factorial(k-1)
}
