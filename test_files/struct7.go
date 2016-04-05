package main

import "fmt"

type person struct {
    name string
    age  int
}

func main() {
	var p person
    p.name = "Sean"
    p.age = 50
    fmt.Println(p.name)
}
