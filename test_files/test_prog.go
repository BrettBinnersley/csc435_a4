package main

import (
	"fmt"
)

func main() {
	i := 42
	var p = &i
  fmt.Println("My favorite numbers are", *p)
}
