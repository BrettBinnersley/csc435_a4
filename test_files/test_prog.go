package main

import "fmt"

func main() {
  var x int
  x = 100
	for sum :=0; sum < x; sum = sum + 1 {
    fmt.Println(sum)
    // x = x + 1
	}
}
