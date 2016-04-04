package main

import "fmt"

func main() {
    test := new(int)
    test2 := new([5]int)
    fmt.Println(test)
    fmt.Printf("%v\n", test2)
}
