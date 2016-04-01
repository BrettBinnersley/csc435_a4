package main

import (
	"fmt"
)

var glob = 97
const k int = 33

func main() {
    var x int
    x = k
    x = x + 1
    glob *= 2
    if x < 0 {
      pi := 3.14159
    	fmt.Println("My favorite numbers are", x, glob, pi)
    } else {
      fmt.Println("Surprise!")
    }
}
