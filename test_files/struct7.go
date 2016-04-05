package main

import "fmt"

type person struct {
    name int
    age  int
}

func main() {
	var p person
    p.name = 20
    p.age = 50
    fmt.Println(p.name)
    fmt.Println(p.age)
}
