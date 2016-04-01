
target datalayout = "e-p:64:64:64-i1:8:8-i8:8:8-i16:16:16-i32:32:32-i64:64:64-f32:32:32-f64:64:64-v64:64:64-v128:128:128-a0:0:64-s0:64:64-f80:128:128-n8:16:32:64-S128"

target triple = "x86_64-unknown-linux-gnu"

@glob.1 = common global i32 97, align 4
@k.2 = constant i32 33, align 4
@.str.3 = private unnamed_addr constant [24 x i8] c"My favorite numbers are\00", align 1
@.str.4 = private unnamed_addr constant [12 x i8] c"%s %d %d %g\00", align 1
@.str.5 = private unnamed_addr constant [10 x i8] c"Surprise!\00", align 1
@.str.6 = private unnamed_addr constant [3 x i8] c"%s\00", align 1
declare i64 @strlen(i8*) #1
declare i8* @malloc(i64) #1
declare i8* @strcpy(i8*, i8*) #1
declare i8* @strcat(i8*, i8*) #1
declare i8* @strncpy(i8*, i8*, i64) #1
declare i64 @printf(i8*, ...) #1
declare i8* @gets(i8*) #1
declare i64 @atoi(i8*) #1
declare void @llvm.memset.p0i8.i64(i8*, i8, i64, i32, i1)

define i8* @String.Concat(i8* %s1, i8* %s2) #0 {
entry:
  %0 = call i64 @strlen(i8* %s1)
  %1 = call i64 @strlen(i8* %s2)
  %2 = add i64 %0, %1
  %3 = add i64 %2, 1
  %4 = call i8* @malloc(i64 %3)
  %5 = call i8* @strcpy(i8* %4, i8* %s1)
  %6 = call i8* @strcat(i8* %5, i8* %s2)
  ret i8* %6
}

define i8* @String.Substring(i8* %s, i32 %start, i32 %end) #0 {
entry:
  %s.addr = alloca i8*, align 8
  %start.addr = alloca i32, align 4
  %end.addr = alloca i32, align 4
  %n = alloca i32, align 4
  %r = alloca i8*, align 8
  store i8* %s, i8** %s.addr, align 8
  store i32 %start, i32* %start.addr, align 4
  store i32 %end, i32* %end.addr, align 4
  %0 = load i32, i32* %end.addr, align 4
  %1 = load i32, i32* %start.addr, align 4
  %sub = sub nsw i32 %0, %1
  %add = add nsw i32 %sub, 1
  store i32 %add, i32* %n, align 4
  %2 = load i32, i32* %n, align 4
  %m = zext i32 %2 to i64
  %call = call i8* @malloc(i64 %m)
  store i8* %call, i8** %r, align 8
  %3 = load i8*, i8** %r, align 8
  %4 = load i8*, i8** %s.addr, align 8
  %5 = load i32, i32* %start.addr, align 8
  %add.ptr = getelementptr inbounds i8, i8* %4, i32 %5
  %6 = load i32, i32* %n, align 4
  %7 = zext i32 %6 to i64
  %call1 = call i8* @strncpy(i8* %3, i8* %add.ptr, i64 %7)
  ret i8* %call1
}

define i8* @String.Substring2(i8* %s, i32 %start) #0 {
entry:
  %s.addr = alloca i8*, align 8
  %start.addr = alloca i32, align 4
  store i8* %s, i8** %s.addr, align 8
  store i32 %start, i32* %start.addr, align 4
  %0 = load i8*, i8** %s.addr, align 8
  %1 = load i32, i32* %start.addr, align 4
  %add.ptr = getelementptr inbounds i8, i8* %0, i32 %1
  ret i8* %add.ptr
}

define i32 @String.Length(i8* %s) #0 {
entry:
  %s.addr = alloca i8*, align 8
  store i8* %s, i8** %s.addr, align 8
  %0 = load i8*, i8** %s.addr, align 8
  %call = call i64 @strlen(i8* %0)
  %1 = trunc i64 %call to i32
  ret i32 %1
}

define i32 @Int32.Parse(i8* %s) #0 {
entry:
  %s.addr = alloca i8*, align 8
  store i8* %s, i8** %s.addr, align 8
  %0 = load i8*, i8** %s.addr, align 8
  %call = call i64 @atoi(i8* %0)
  %1 = trunc i64 %call to i32
  ret i32 %1
}
define i8* @Console.ReadLine() #0 {
entry:
  %buff = alloca [80 x i8], align 1
  %s = alloca i8*, align 8
  %arraydecay = getelementptr inbounds [80 x i8], [80 x i8]* %buff, i32 0, i32 0
  %call = call i8* @gets(i8* %arraydecay)
  %cmp = icmp ne i8* %call, null
  br i1 %cmp, label %if.then, label %if.end
if.then:
  %call1 = call i64 @strlen(i8* %call)
  %add = add i64 %call1, 1
  %call2 = call i8* @malloc(i64 %add)
  %call3 = call i8* @strcpy(i8* %call2, i8* %call)
  br label %if.end
if.end:
  %0 = phi i8* [%call, %entry], [%call3, %if.then]
  ret i8* %0
}

; Function Attrs: nounwind uwtable
define void @main() {
entry:
  %0 = alloca i32, align 8 ; x
  %1 = load i32, i32* @k.2
  store i32 %1, i32* %0, align 4
  %2 = load i32, i32* %0
  %3 = add i32 %2, 1
  store i32 %3, i32* %0, align 4
  %4 = load i32, i32* @glob.1
  %5 = mul i32 %4, 2
  store i32 %5, i32* @glob.1, align 4
  %6 = load i32, i32* %0
  %7 = icmp sgt i32 %6, 0
  br i1 %7, label %then.0, label %else.1
then.0:
  %8 = alloca double, align 8 ; pi
  store double 3.14159, double* %8
  %9 = load i32, i32* %0
  %10 = load i32, i32* @glob.1
  %11 = load double, double* %8
  %12 = call i64 (i8*, ...) @printf(i8* getelementptr inbounds ([12 x i8], [12 x i8]* @.str.4, i32 0, i32 0) , [24 x i8]* @.str.3, i32 %9, i32 %10, double %11)
  br label %endif.2
else.1:
  %13 = call i64 (i8*, ...) @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str.6, i32 0, i32 0) , [10 x i8]* @.str.5)
  br label %endif.2
endif.2:
  ret void
}


attributes #0 = { nounwind uwtable "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #1 = { "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "unsafe-fp-math"="false" "use-soft-float"="false" }

