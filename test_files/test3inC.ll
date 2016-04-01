; ModuleID = 'test3inC.c'
target datalayout = "e-m:w-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-w64-windows-gnu"

@.str = private unnamed_addr constant [5 x i8] c"%s \0A\00", align 1
@.str.1 = private unnamed_addr constant [9 x i8] c"all done\00", align 1
@arr = common global [10 x float] zeroinitializer, align 16
@.str.2 = private unnamed_addr constant [11 x i8] c"%s %d %g \0A\00", align 1
@.str.3 = private unnamed_addr constant [12 x i8] c"k, arr[k] =\00", align 1

; Function Attrs: nounwind uwtable
define i32 @main() #0 {
entry:
  %retval = alloca i32, align 4
  store i32 0, i32* %retval
  call void @fill(i32 10)
  call void @dump(i32 10)
  %call = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([5 x i8], [5 x i8]* @.str, i32 0, i32 0), i8* getelementptr inbounds ([9 x i8], [9 x i8]* @.str.1, i32 0, i32 0))
  ret i32 0
}

; Function Attrs: nounwind uwtable
define void @fill(i32 %k) #0 {
entry:
  %k.addr = alloca i32, align 4
  store i32 %k, i32* %k.addr, align 4
  %0 = load i32, i32* %k.addr, align 4
  %cmp = icmp sge i32 %0, 0
  br i1 %cmp, label %if.then, label %if.end

if.then:                                          ; preds = %entry
  %1 = load i32, i32* %k.addr, align 4
  %sub = sub nsw i32 %1, 1
  store i32 %sub, i32* %k.addr, align 4
  %2 = load i32, i32* %k.addr, align 4
  %3 = load i32, i32* %k.addr, align 4
  %mul = mul nsw i32 %2, %3
  %conv = sitofp i32 %mul to float
  %4 = load i32, i32* %k.addr, align 4
  %idxprom = sext i32 %4 to i64
  %arrayidx = getelementptr inbounds [10 x float], [10 x float]* @arr, i32 0, i64 %idxprom
  store float %conv, float* %arrayidx, align 4
  %5 = load i32, i32* %k.addr, align 4
  call void @fill(i32 %5)
  br label %if.end

if.end:                                           ; preds = %if.then, %entry
  ret void
}

; Function Attrs: nounwind uwtable
define void @dump(i32 %k) #0 {
entry:
  %k.addr = alloca i32, align 4
  store i32 %k, i32* %k.addr, align 4
  %0 = load i32, i32* %k.addr, align 4
  %cmp = icmp sgt i32 %0, 0
  br i1 %cmp, label %if.then, label %if.end

if.then:                                          ; preds = %entry
  %1 = load i32, i32* %k.addr, align 4
  %sub = sub nsw i32 %1, 1
  store i32 %sub, i32* %k.addr, align 4
  %2 = load i32, i32* %k.addr, align 4
  call void @dump(i32 %2)
  %3 = load i32, i32* %k.addr, align 4
  %4 = load i32, i32* %k.addr, align 4
  %idxprom = sext i32 %4 to i64
  %arrayidx = getelementptr inbounds [10 x float], [10 x float]* @arr, i32 0, i64 %idxprom
  %5 = load float, float* %arrayidx, align 4
  %conv = fpext float %5 to double
  %call = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([11 x i8], [11 x i8]* @.str.2, i32 0, i32 0), i8* getelementptr inbounds ([12 x i8], [12 x i8]* @.str.3, i32 0, i32 0), i32 %3, double %conv)
  br label %if.end

if.end:                                           ; preds = %if.then, %entry
  ret void
}

declare i32 @printf(i8*, ...) #1

attributes #0 = { nounwind uwtable "disable-tail-calls"="false" "less-precise-fpmad"="false" "no-frame-pointer-elim"="false" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="x86-64" "target-features"="+sse,+sse2" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #1 = { "disable-tail-calls"="false" "less-precise-fpmad"="false" "no-frame-pointer-elim"="false" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="x86-64" "target-features"="+sse,+sse2" "unsafe-fp-math"="false" "use-soft-float"="false" }

!llvm.module.flags = !{!0}
!llvm.ident = !{!1}

!0 = !{i32 1, !"PIC Level", i32 2}
!1 = !{!"clang version 3.7.0 (tags/RELEASE_370/final)"}
