package main

import (
	"fmt"
)

func main() {
    // y = true
    y := 10
    if(!(y == 10)) {
      fmt.Println("My favorite numbers are", y)
    } else {
      fmt.Println("Surpise!", y)
    }
}
